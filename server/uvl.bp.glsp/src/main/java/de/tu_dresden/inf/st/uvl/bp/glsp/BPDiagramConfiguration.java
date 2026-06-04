/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp;

import de.tu_dresden.inf.st.uvl.glsp.UVLDiagramConfiguration;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.server.types.ShapeTypeHint;

public class BPDiagramConfiguration extends UVLDiagramConfiguration {

  @Override
  public List<ShapeTypeHint> getShapeTypeHints() {
    List<ShapeTypeHint> shapeTypeHints = new ArrayList<>(super.getShapeTypeHints());

    replaceTypeHints(
        shapeTypeHints,
        List.of(
            new ShapeTypeHint(
                DefaultTypes.GRAPH,
                false,
                false,
                false,
                false,
                List.of(UVLModelTypes.FEATURE, BPModelTypes.B_THREAD, BPModelTypes.BP_CONTEXT)),
            new ShapeTypeHint(
                UVLModelTypes.FEATURE,
                true,
                true,
                false,
                true,
                List.of(
                    UVLModelTypes.FEATURE,
                    BPModelTypes.B_THREAD,
                    UVLModelTypes.ATTRIBUTE,
                    UVLModelTypes.CARDINALITY_LABEL))));

    shapeTypeHints.addAll(
        List.of(
            new ShapeTypeHint(
                BPModelTypes.B_THREAD,
                true,
                true,
                false,
                true,
                List.of(
                    UVLModelTypes.ATTRIBUTE,
                    UVLModelTypes.CARDINALITY_LABEL,
                    BPModelTypes.REQUESTED_EVENT,
                    BPModelTypes.BLOCKED_EVENT,
                    BPModelTypes.WAITED_FOR_EVENT)),
            new ShapeTypeHint(
                BPModelTypes.REQUESTED_EVENT,
                true,
                true,
                false,
                false,
                List.of(BPModelTypes.BLOCKED_EVENT, BPModelTypes.WAITED_FOR_EVENT)),
            new ShapeTypeHint(
                BPModelTypes.BLOCKED_EVENT,
                true,
                true,
                false,
                false,
                List.of(BPModelTypes.REQUESTED_EVENT, BPModelTypes.WAITED_FOR_EVENT)),
            new ShapeTypeHint(
                BPModelTypes.WAITED_FOR_EVENT,
                true,
                true,
                false,
                false,
                List.of(BPModelTypes.REQUESTED_EVENT, BPModelTypes.BLOCKED_EVENT)),
            new ShapeTypeHint(
                BPModelTypes.BP_CONFIG, true, true, false, true, List.of(UVLModelTypes.ATTRIBUTE)),
            new ShapeTypeHint(
                BPModelTypes.BP_ENV, true, true, false, true, List.of(UVLModelTypes.ATTRIBUTE))));
    return shapeTypeHints;
  }
}
