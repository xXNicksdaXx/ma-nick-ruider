/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.vill.model.Feature;
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
        GModelElement parentElement = findParent(label);
        Object uvlObject = modelState.getIndex().getUVLObject(parentElement).orElseThrow(
                () -> new IllegalArgumentException("No UVL object found for the given parent node."));

        // update label for each type
        if (uvlObject instanceof Feature feature) {
            updateFeatureName(label, feature, operation.getText());
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

    protected GModelElement findParent(final GLabel label) {
        // remove "_label"
        String elementId = label.getId().substring(0, label.getId().length() - 6);

        // Traverse up the GModel to find the parent element
        GModelElement parent = label.getParent();
        while (parent != null) {
            if (parent.getId().equals(elementId)) {
                return parent;
            }
            parent = parent.getParent();
        }
        throw new IllegalArgumentException("Parent node for label with ID " + label.getId() + " not found.");
    }
}
