/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.graph.*;
import org.eclipse.glsp.server.features.directediting.ApplyLabelEditOperation;
import org.eclipse.glsp.server.gmodel.GModelApplyLabelEditOperationHandler;

import java.util.Objects;
import java.util.Optional;

import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelEditUtil.renameFeature;

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
        GNode parentNode = findParentNode(operation);
        Object uvlObject = modelState.getIndex().getUVLObject(parentNode).orElseThrow(
                () -> new IllegalArgumentException("No UVL object found for the given parent node."));

        if (uvlObject instanceof Feature feature) {
            // Update GModel
            GLabel label = findLabel(operation).orElseThrow(
                    () -> new IllegalArgumentException("Element with provided ID cannot be found or is not a GLabel"));
            label.setText(operation.getText());

            // Update FeatureModel
            FeatureModel featureModel = renameFeature(modelState.getFeatureModel(), feature, operation.getText());
            modelState.setFeatureModel(featureModel);
            modelState.updateRoot(modelState.getRoot());
        } else {
            throw new IllegalArgumentException("Parent node does not correspond to a UVL Feature.");
        }
    }

    protected GNode findParentNode(final ApplyLabelEditOperation operation) {
        GLabel label = findLabel(operation).orElseThrow(
                () -> new IllegalArgumentException("Element with provided ID cannot be found or is not a GLabel"));
        String elementId = label.getId().substring(0, label.getId().length() - 6);
        GModelElement parent = label.getParent();
        while (parent != null) {
            if (parent.getId().equals(elementId) && parent instanceof GNode) {
                return (GNode) parent;
            }
            parent = parent.getParent();
        }
        throw new IllegalArgumentException("Parent node for label with ID " + label.getId() + " not found.");
    }

}
