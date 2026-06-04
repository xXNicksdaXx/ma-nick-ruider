/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.palette;

import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.palette.UVLToolPaletteItemProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.glsp.server.features.toolpalette.PaletteItem;

public class BPToolPaletteItemProvider extends UVLToolPaletteItemProvider {

  @Override
  public List<PaletteItem> getItems(Map<String, String> args) {
    List<PaletteItem> toolPaletteItems = new ArrayList<>(super.getItems(args));
    toolPaletteItems.add(bpElements());
    return toolPaletteItems;
  }

  protected PaletteItem bpElements() {
    List<PaletteItem> bpElements =
        List.of(
            node(BPModelTypes.B_THREAD, "B-Thread"),
            node(BPModelTypes.REQUESTED_EVENT, "Requested Event"),
            node(BPModelTypes.BLOCKED_EVENT, "Blocked Event"),
            node(BPModelTypes.WAITED_FOR_EVENT, "Waited-for Event"),
            node(BPModelTypes.BP_CONTEXT, "BP Context"));
    return PaletteItem.createPaletteGroup(
        "bpElements", "BP Elements", bpElements, "symbol-property", "AA");
  }
}
