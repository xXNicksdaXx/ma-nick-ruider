/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.glsp.server.actions.AbstractActionHandler;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionDispatcher;

/** Dispatches {@link HighlightElementAction} actions to the client. */
@Singleton
public class HighlightElementActionHandler extends AbstractActionHandler<HighlightElementAction> {

  @Inject protected ActionDispatcher actionDispatcher;

  @Inject protected UVLModelState modelState;

  @Override
  protected List<Action> executeAction(final HighlightElementAction action) {
    Set<String> existingIds = existingModelIds(action.getElementIds());

    if (!existingIds.isEmpty()) {
      actionDispatcher.dispatchAfterNextUpdate(
          new HighlightElementAction(List.copyOf(existingIds), action.isHighlighted()));
    }

    return listOf();
  }

  protected Set<String> existingModelIds(final Collection<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return Set.of();
    }

    Set<String> existingIds = new LinkedHashSet<>();
    for (String id : ids) {
      if (id == null) {
        continue;
      }
      String trimmedId = id.trim();
      if (trimmedId.isEmpty()) {
        continue;
      }

      if (modelState.getIndex().getGModelElement(trimmedId).isPresent()) {
        existingIds.add(trimmedId);
      }
    }
    return existingIds;
  }
}
