/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.actions;

import java.util.Collections;
import java.util.List;
import org.eclipse.glsp.server.actions.Action;

public class HighlightElementAction extends Action {

  public static final String KIND = "highlightElement";

  private List<String> elementIds = Collections.emptyList();
  private boolean isHighlighted;

  public HighlightElementAction() {
    super(KIND);
  }

  public HighlightElementAction(List<String> elementIds, boolean isHighlighted) {
    super(KIND);
    this.elementIds = elementIds;
    this.isHighlighted = isHighlighted;
  }

  public HighlightElementAction(String elementId, boolean isHighlighted) {
    this(Collections.singletonList(elementId), isHighlighted);
  }

  @Override
  public String getKind() {
    return KIND;
  }

  public List<String> getElementIds() {
    return elementIds;
  }

  public void setElementIds(List<String> elementIds) {
    this.elementIds = elementIds;
  }

  public String getElementId() {
    return elementIds != null && !elementIds.isEmpty() ? elementIds.getFirst() : null;
  }

  public void setElementId(String elementId) {
    this.elementIds = Collections.singletonList(elementId);
  }

  public boolean isHighlighted() {
    return isHighlighted;
  }

  public void setHighlighted(boolean isHighlighted) {
    this.isHighlighted = isHighlighted;
  }
}
