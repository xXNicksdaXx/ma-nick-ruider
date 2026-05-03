package de.tu_dresden.inf.st.uvl.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.LiteralConstraint;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.server.gmodel.GModelCreateOperationHandler;
import org.eclipse.glsp.server.operations.CreateNodeOperation;

public class UVLCreateComplexConstraintOperationHandler
    extends GModelCreateOperationHandler<CreateNodeOperation> {

  @Inject protected UVLModelState modelState;

  UVLCreateComplexConstraintOperationHandler() {
    super(UVLModelTypes.CONSTRAINT);
  }

  @Override
  public Optional<Command> createCommand(CreateNodeOperation operation) {
    return commandOf(() -> executeCreation(operation));
  }

  protected void executeCreation(CreateNodeOperation operation) {
    // find parent feature
    String containerId = operation.getContainerId();
    if (!containerId.equals("constraint_box")) {
      return;
    }

    Feature rootFeature = modelState.getFeatureModel().getRootFeature();
    if (rootFeature == null) {
      return;
    }

    Constraint tempConstraint = new LiteralConstraint(rootFeature);

    modelState.getFeatureModel().getOwnConstraints().stream()
        .filter(constraint -> constraint.toString().equals(tempConstraint.toString()))
        .findFirst()
        .ifPresentOrElse(
            _ -> {},
            () -> {
              // if no such constraint exists, add the new constraint to the model
              modelState.getFeatureModel().getOwnConstraints().add(tempConstraint);
            });

    // update the model index
    modelState.updateIndex();
  }
}
