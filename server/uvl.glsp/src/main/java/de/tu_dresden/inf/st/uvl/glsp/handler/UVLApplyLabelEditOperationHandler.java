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
            switch (label.getType()) {
                case UVLModelTypes.FEATURE_NAME -> updateFeatureName(label, feature, operation.getText());
                case UVLModelTypes.ATTRIBUTE_NAME -> updateAttributeName(label, feature, operation.getText());
                case UVLModelTypes.ATTRIBUTE_VALUE -> updateAttributeValue(label, feature, operation.getText());
                case UVLModelTypes.CARDINALITY_LABEL -> updateFeatureCardinality(label, feature, operation.getText());
                default -> throw new IllegalArgumentException("Label type " + label.getType() + " is not supported for Feature elements.");
            }
        } else if (uvlObject instanceof Group group) {
            updateGroupCardinality(label, group, operation.getText());
        } else {
            throw new IllegalArgumentException("Parent node does not correspond to a UVL Feature.");
        }

        // update the model index
        modelState.updateIndex();
    }

    protected void updateFeatureName(GLabel label, Feature feature, String newName) {
        // update GModel
        label.setText(newName);

        // update Feature
        feature.setFeatureName(newName);
    }

    protected void  updateAttributeName(GLabel label, Feature feature, String newName) {
        // get previous attribute name & value
        String previousName = label.getText();
        Attribute<?> previousAttribute = feature.getAttributes().get(previousName);
        if (previousAttribute == null) {
            throw new IllegalArgumentException("No attribute found for name " + previousName + " in feature " + feature.getFeatureName());
        }

        // update GModel
        label.setText(newName);

        // update Feature Attribute
        Attribute<?> newAttribute = new Attribute<>(newName, previousAttribute.getValue(), feature);
        feature.getAttributes().putIfAbsent(newName, newAttribute);
        feature.getAttributes().remove(previousName);

        // update expression references if the attribute is used in any constraints
        if (ConstraintUtil.featureAttributeIsInConstraint(feature, previousName, modelState.getFeatureModel())) {
            ConstraintUtil.updateFeatureAttributeInConstraints(feature, previousName, newName, modelState.getFeatureModel());
        }
    }

    protected void updateAttributeValue(GLabel label, Feature feature, String newValue) {
        // get attribute index from label ID
        int index = GModelUtil.extractAttributeIndex(label.getId());
        if (index == -1) {
            throw new IllegalArgumentException("No valid attribute index found in label ID: " + label.getId());
        }

        if (index >= feature.getAttributes().size()) {
            throw new IllegalArgumentException("Attribute index " + index + " is out of bounds for feature with " + feature.getAttributes().size() + " attributes.");
        }

        String attributeKey = feature.getAttributes().keySet().stream().skip(index).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Attribute index " + index + " is out of bounds for feature with " + feature.getAttributes().size() + " attributes."));
        Attribute<?> previousAttribute = feature.getAttributes().get(attributeKey);
        if (previousAttribute == null) {
            throw new IllegalArgumentException("No attribute found for key " + attributeKey + " in feature " + feature.getFeatureName());
        }

        // update GModel
        label.setText(newValue);

        // update Feature Attribute
        Attribute<?> newAttribute = new Attribute<>(
                previousAttribute.getName(),
                TypeCastingUtil.convertStringToBestType(newValue),
                feature
        );
        feature.getAttributes().replace(attributeKey, newAttribute);
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
