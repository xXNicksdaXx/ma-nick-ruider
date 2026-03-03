/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.EquivalenceConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.ImplicationConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.LiteralConstraint;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.server.gmodel.GModelCreateOperationHandler;
import org.eclipse.glsp.server.operations.CreateEdgeOperation;

import java.util.Optional;

public class UVLCreateBiConstraintEdgeOperationHandler extends GModelCreateOperationHandler<CreateEdgeOperation> {

    @Inject
    protected UVLModelState modelState;

    UVLCreateBiConstraintEdgeOperationHandler() {
        super(UVLModelTypes.EQUIVALENCE, UVLModelTypes.IMPLICATION);
    }

    @Override
    public Optional<Command> createCommand(CreateEdgeOperation operation) {
        return edgeAlreadyExists(operation, operation.getElementTypeId())
                ? doNothing()
                : commandOf(() -> executeCreation(operation));
    }

    protected void executeCreation(CreateEdgeOperation operation) {
        LiteralConstraint sourceConstraint = getSourceConstraint(operation);
        LiteralConstraint targetConstraint = getTargetConstraint(operation);

        // add the new constraints to the feature model
        modelState.getFeatureModel().getLiteralConstraints().add(sourceConstraint);
        modelState.getFeatureModel().getLiteralConstraints().add(targetConstraint);

        if (operation.getElementTypeId().equals(UVLModelTypes.EQUIVALENCE)) {
            modelState.getFeatureModel().getOwnConstraints().add(new EquivalenceConstraint(sourceConstraint, targetConstraint));
        } else if (operation.getElementTypeId().equals(UVLModelTypes.IMPLICATION)) {
            modelState.getFeatureModel().getOwnConstraints().add(new ImplicationConstraint(sourceConstraint, targetConstraint));
        } else {
            throw new IllegalArgumentException("Unsupported edge type: " + operation.getElementTypeId());
        }

        // update the model index
        modelState.updateIndex();
    }

    protected LiteralConstraint getSourceConstraint(CreateEdgeOperation operation) {
        Feature source = modelState.getIndex()
                .getUVLObject(operation.getSourceElementId(), Feature.class)
                .orElseThrow(
                        () -> new IllegalStateException("Source feature not found for ID: " + operation.getSourceElementId()));

        return new LiteralConstraint(source);
    }

    protected LiteralConstraint getTargetConstraint(CreateEdgeOperation operation) {
        Feature target = modelState.getIndex()
                .getUVLObject(operation.getTargetElementId(), Feature.class)
                .orElseThrow(
                        () -> new IllegalStateException("Target feature not found for ID: " + operation.getTargetElementId()));

        return new LiteralConstraint(target);
    }

    protected Optional<GEdge> findExistingEdge(CreateEdgeOperation operation) {
        return modelState.getIndex().getAllByClass(GEdge.class).stream().filter(
                gEdge -> gEdge.getSourceId().equals(operation.getSourceElementId())
                        && gEdge.getTargetId().equals(operation.getTargetElementId())).findFirst();
    }

    private boolean edgeAlreadyExists(CreateEdgeOperation operation, String uvlModelType) {
        Optional<GEdge> existingEdge = findExistingEdge(operation);
        return existingEdge.map(gEdge -> gEdge.getType().equals(uvlModelType)).orElse(false);
    }
}
