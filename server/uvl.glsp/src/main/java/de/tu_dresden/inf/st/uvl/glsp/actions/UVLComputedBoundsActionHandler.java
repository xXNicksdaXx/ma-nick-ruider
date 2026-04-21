package de.tu_dresden.inf.st.uvl.glsp.actions;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.SaveModelAction;
import org.eclipse.glsp.server.features.core.model.ComputedBoundsAction;
import org.eclipse.glsp.server.features.core.model.ComputedBoundsActionHandler;
import org.eclipse.glsp.server.features.core.model.SourceModelStorage;
import org.eclipse.glsp.server.layout.LayoutEngine;
import org.eclipse.glsp.server.operations.LayoutOperation;
import org.eclipse.glsp.server.utils.LayoutUtil;

public class UVLComputedBoundsActionHandler extends ComputedBoundsActionHandler {

  private static final long LAYOUT_THRESHOLD_AT_ORIGIN = 3;

  @Inject protected SourceModelStorage sourceModelStorage;

  @Inject protected LayoutEngine layoutEngine;

  @Override
  public List<Action> executeAction(final ComputedBoundsAction action) {
    synchronized (submissionHandler.getModelLock()) {
      GModelRoot model = modelState.getRoot();
      if (model != null
          && action.getRevision().isPresent()
          && action.getRevision().get().doubleValue() == model.getRevision()) {
        LayoutUtil.applyBounds(model, action, modelState);

        if (requiresRelayout(model)) {
          layoutEngine.layout(Optional.of(new LayoutOperation()));
        }

        sourceModelStorage.saveSourceModel(new SaveModelAction());
        return submissionHandler.submitModelDirectly();
      }
    }
    return none();
  }

  protected boolean requiresRelayout(final GModelRoot root) {
    return modelState
            .getIndex()
            .getStream(root)
            .filter(GNode.class::isInstance)
            .map(GNode.class::cast)
            .filter(node -> node.getPosition() != null)
            .filter(node -> node.getPosition().getX() == 0 && node.getPosition().getY() == 0)
            .count()
        > LAYOUT_THRESHOLD_AT_ORIGIN;
  }
}
