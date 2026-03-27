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
import de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil;
import de.tu_dresden.inf.st.uvl.glsp.utils.GModelUtil;
import de.tu_dresden.inf.st.uvl.glsp.utils.TypeCastingUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.*;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.server.features.directediting.ApplyLabelEditOperation;
import org.eclipse.glsp.server.gmodel.GModelApplyLabelEditOperationHandler;

import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;

public class UVLApplyLabelEditOperationHandler extends GModelApplyLabelEditOperationHandler {

    @Inject
    UVLModelState modelState;

    @Override
    public Optional<Command> createCommand(final ApplyLabelEditOperation operation) {
        GLabel label = findLabel(operation).orElseThrow(
                () -> new IllegalArgumentException("Element with provided ID cannot be found or is not a GLabel"));
        return Objects.equals(label.getText(), operation.getText())
                ? doNothing()
                : commandOf(() -> executeLabelEdit(operation));
    }

    protected void executeLabelEdit(final ApplyLabelEditOperation operation) {
        // find parent element of the label and its corresponding UVL object
        GLabel label = findLabel(operation).orElseThrow(
                () -> new IllegalArgumentException("Element with provided ID cannot be found or is not a GLabel"));
        GModelElement parentElement = GModelUtil.findParent(label);
        UVLObject uvlObject = modelState.getIndex().getUVLObject(parentElement.getId())
                .orElseGet(() -> modelState.getIndex()
                        .getUVLObject(parentElement.getId().split("_")[0])
                        .orElseThrow(() -> new IllegalArgumentException("No UVL object found for parent element with ID " + parentElement.getId()))
                );

        // update label for each type
        if (uvlObject instanceof Feature feature) {
            handleFeatureLabelEdit(label, feature, operation.getText());
        } else if (uvlObject instanceof Group group) {
            updateGroupCardinality(label, group, operation.getText());
        } else {
            throw new IllegalArgumentException("Parent node does not correspond to a UVL Feature.");
        }

        // update the model index
        modelState.updateIndex();
    }

    protected void handleFeatureLabelEdit(final GLabel label, final Feature feature, final String newText) {
        switch (label.getType()) {
            case UVLModelTypes.FEATURE_NAME -> updateFeatureName(label, feature, newText);
            case UVLModelTypes.ATTRIBUTE_NAME -> updateAttributeName(label, feature, newText);
            case UVLModelTypes.ATTRIBUTE_VALUE -> updateAttributeValue(label, feature, newText);
            case UVLModelTypes.CARDINALITY_LABEL -> updateFeatureCardinality(label, feature, newText);
            default -> throw new IllegalArgumentException("Label type " + label.getType() + " is not supported for Feature elements.");
        }
    }

    protected void updateFeatureName(GLabel label, Feature feature, String newName) {
        // update GModel
        label.setText(newName);

        // update Feature
        feature.setFeatureName(newName);
    }

    protected void  updateAttributeName(GLabel label, Feature feature, String newName) {
        GModelUtil.ResolvedAttribute resolvedAttribute = GModelUtil.resolveAttribute(feature, label.getId())
                .orElseThrow(() -> new IllegalArgumentException("No attribute found for label ID: " + label.getId()));

        String previousName = resolvedAttribute.attribute().getName();
        if (resolvedAttribute.parentMap().containsKey(newName) && !Objects.equals(newName, resolvedAttribute.mapKey())) {
            throw new IllegalArgumentException("An attribute with the name '" + newName + "' already exists at this level.");
        }

        // update GModel
        label.setText(newName);

        // update Feature Attribute while preserving insertion order
        Attribute<?> newAttribute = new Attribute<>(newName, resolvedAttribute.attribute().getValue(), feature);
        replaceMapEntry(resolvedAttribute.parentMap(), resolvedAttribute.mapKey(), newName, newAttribute);

        // Constraints can only reference top-level attributes via feature.attribute
        if (resolvedAttribute.path().size() == 1
                && ConstraintUtil.featureAttributeIsInConstraint(feature, previousName, modelState.getFeatureModel())) {
            ConstraintUtil.updateFeatureAttributeInConstraints(feature, previousName, newName, modelState.getFeatureModel());
        }
    }

    protected void updateAttributeValue(GLabel label, Feature feature, String newValue) {
        GModelUtil.ResolvedAttribute resolvedAttribute = GModelUtil.resolveAttribute(feature, label.getId())
                .orElseThrow(() -> new IllegalArgumentException("No attribute found for label ID: " + label.getId()));

        // update GModel
        label.setText(newValue);

        // update Feature Attribute
        Attribute<?> previousAttribute = resolvedAttribute.attribute();
        Attribute<Object> updatedAttribute = new Attribute<>(
                previousAttribute.getName(),
                TypeCastingUtil.convertStringToBestType(newValue),
                feature
        );
        replaceMapEntry(resolvedAttribute.parentMap(), resolvedAttribute.mapKey(), resolvedAttribute.mapKey(), updatedAttribute);
    }

    protected void replaceMapEntry(final Map<String, Attribute<?>> attributes, final String oldKey,
                                   final String newKey, final Attribute<?> attribute) {
        if (Objects.equals(oldKey, newKey)) {
            attributes.put(oldKey, attribute);
            return;
        }

        Map<String, Attribute<?>> reorderedAttributes = new LinkedHashMap<>();
        for (Map.Entry<String, Attribute<?>> entry : attributes.entrySet()) {
            if (entry.getKey().equals(oldKey)) {
                reorderedAttributes.put(newKey, attribute);
            } else {
                reorderedAttributes.put(entry.getKey(), entry.getValue());
            }
        }
        attributes.clear();
        attributes.putAll(reorderedAttributes);
    }

    protected void updateFeatureCardinality(GLabel label, Feature feature, String newName) {
        if (newName.isBlank()) {
            // remove feature cardinality if the new name is empty
            feature.setCardinality(null);

            // remove language level if no other feature uses cardinality
            if (!FeatureModelUtil.includesFeatureCardinality(modelState.getFeatureModel())) {
                modelState.getFeatureModel().getUsedLanguageLevels().remove(LanguageLevel.FEATURE_CARDINALITY);
            }
            return;
        }

        // check if the new name is a valid cardinality (e.g., "0..*", "1..1", etc.)
        if (!newName.matches("\\d+\\.\\.(\\d+|\\*)")) {
            throw new IllegalArgumentException("Invalid cardinality format: " + newName);
        }

        // update GModel
        label.setText(newName);

        // update Feature
        Cardinality cardinality = FeatureModelUtil.createCardinality(newName);
        feature.setCardinality(cardinality);
    }

    protected void updateGroupCardinality(GLabel label, Group group, String newName) {
        // check if the new name is a valid cardinality (e.g., "0..*", "1..1", etc.)
        if (!newName.matches("\\d+\\.\\.(\\d+|\\*)")) {
            throw new IllegalArgumentException("Invalid cardinality format: " + newName);
        }

        // update GModel
        label.setText(newName);

        // update Group
        Cardinality cardinality = FeatureModelUtil.createCardinality(newName);
        group.setCardinality(cardinality);
    }


}
