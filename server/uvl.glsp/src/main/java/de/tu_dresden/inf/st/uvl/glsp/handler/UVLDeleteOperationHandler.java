/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.glsp.utils.ConstraintUtil;
import de.tu_dresden.inf.st.uvl.glsp.utils.GModelUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GModelIndex;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.server.operations.DeleteOperation;
import org.eclipse.glsp.server.operations.GModelOperationHandler;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UVLDeleteOperationHandler extends GModelOperationHandler<DeleteOperation> {
    protected static Logger LOGGER = LogManager.getLogger(UVLDeleteOperationHandler.class);

    @Inject
    UVLModelState modelState;

    @Override
    public Optional<Command> createCommand(final DeleteOperation operation) {
        List<String> elementIds = operation.getElementIds();
        if (elementIds == null || elementIds.isEmpty()) {
            LOGGER.warn("Elements to delete are not specified");
            return doNothing();
        }
        return commandOf(() -> deleteElements(elementIds));
    }

    public void deleteElements(final List<String> elementIds) {
        boolean success = elementIds.stream().allMatch(this::delete);
        if (!success) {
            LOGGER.warn("Could not delete all elements as requested (see messages above to find out why)");
        }
    }

    protected boolean delete(final String elementId) {
        Optional<Object> element = modelState.getIndex().getUVLObject(elementId);
        if (element.isEmpty()) {
            return deleteSubElement(elementId);
        }

        Set<Object> objectsToDelete = new LinkedHashSet<>();
        collectUvlDependents(objectsToDelete, element.get());

        objectsToDelete.forEach(this::deleteUvlObject);

        Optional<GModelElement> gModelElement = modelState.getIndex().getGModelElement(elementId);
        if (gModelElement.isEmpty()) {
            LOGGER.warn("GModel Element not found: {}", elementId);
            return false;
        }

        Set<GModelElement> elementsToDelete = objectsToDelete.stream().map(modelState.getIndex()::getGModelElement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        collectGModelDependents(elementsToDelete, gModelElement.get());

        elementsToDelete.forEach(EcoreUtil::delete);

        modelState.updateIndex();
        return true;
    }

    protected void deleteUvlObject(final Object uvlObject) {
        switch (uvlObject) {
            case Feature feature -> {
                Group parentGroup = feature.getParentGroup();
                if (parentGroup != null) {
                    parentGroup.getFeatures().remove(feature);
                    if (parentGroup.getFeatures().isEmpty()) {
                        parentGroup.getParentFeature().getChildren().remove(parentGroup);
                    }
                }
                modelState.getFeatureModel().getFeatureMap().remove(feature.getFeatureName(), feature);
            }
            case Constraint constraint -> modelState.getFeatureModel().getOwnConstraints().remove(constraint);
            default -> LOGGER.warn("Unknown UVL object type for deletion: {}", uvlObject.getClass().getName());
        }
        modelState.updateIndex();
    }

    protected boolean deleteSubElement(final String elementId) {
        Optional<GModelElement> gModelElement = modelState.getIndex().getGModelElement(elementId);
        if (gModelElement.isEmpty()) {
            LOGGER.warn("GModel Element not found: {}", elementId);
            return false;
        }

        GModelElement element = gModelElement.get();
        if (element.getType().equals(UVLModelTypes.ATTRIBUTE)) {
            int index = GModelUtil.extractAttributeIndex(elementId);
            String featureId = GModelUtil.extractUUID(elementId);

            Feature feature = modelState.getIndex().getUVLObject(featureId, Feature.class).orElseThrow(
                    () -> new IllegalStateException("Feature not found for attribute: " + elementId));
            String attributeName = feature.getAttributes().keySet().stream().skip(index).findFirst().orElse(null);
            feature.getAttributes().remove(attributeName);

            if (ConstraintUtil.featureAttributeIsInConstraint(feature, attributeName, modelState.getFeatureModel())) {
                ConstraintUtil.removeFeatureAttributeInConstraints(feature, attributeName, modelState.getFeatureModel());
            }
        }
        return true;
    }

    protected void collectUvlDependents(final Set<Object> dependents, final Object objectToDelete) {
        if (dependents.contains(objectToDelete)) {
            // Already collected as dependent of another object, no need to collect again
            return;
        }

        if (objectToDelete instanceof Feature feature) {
            feature.getChildren().stream()
                    .flatMap(group -> group.getFeatures().stream())
                    .forEach(childFeature -> collectUvlDependents(dependents, childFeature));
            if (ConstraintUtil.featureIsInConstraint(feature, modelState.getFeatureModel())) {
                ConstraintUtil.getConstraintsForFeature(feature, modelState.getFeatureModel())
                        .forEach(constraint -> collectUvlDependents(dependents, constraint));
            }
        }

        dependents.add(objectToDelete);
    }

    protected void collectGModelDependents(final Set<GModelElement> dependents, final GModelElement nodeToDelete) {
        if (dependents.contains(nodeToDelete)) {
            // Already collected as dependent of another node, no need to collect again
            return;
        }

        // Recursively collect child nodes (if any)
        if (nodeToDelete.getChildren() != null) {
            for (GModelElement child : nodeToDelete.getChildren()) {
                collectGModelDependents(dependents, child);
            }
        }

        // Collect connected edges if the node is a GNode
        if (nodeToDelete instanceof GNode) {
            GModelIndex index = modelState.getIndex();
            dependents.addAll(index.getIncomingEdges(nodeToDelete));
            dependents.addAll(index.getOutgoingEdges(nodeToDelete));
        }

        dependents.add(nodeToDelete);
    }
}
