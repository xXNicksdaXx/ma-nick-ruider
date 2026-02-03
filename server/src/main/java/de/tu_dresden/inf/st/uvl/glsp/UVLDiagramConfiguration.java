/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp;

import java.util.List;

import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.server.diagram.BaseDiagramConfiguration;
import org.eclipse.glsp.server.types.EdgeTypeHint;
import org.eclipse.glsp.server.types.ShapeTypeHint;

public class UVLDiagramConfiguration extends BaseDiagramConfiguration {

        @Override
        public List<ShapeTypeHint> getShapeTypeHints() {
                return List.of(
                        new ShapeTypeHint(DefaultTypes.GRAPH,
                                false, false, false, false,
                                List.of(UVLModelTypes.FEATURE)),
                        new ShapeTypeHint(UVLModelTypes.FEATURE,
                                true, true, false, true,
                                List.of(UVLModelTypes.FEATURE))
                );
        }

        @Override
        public List<EdgeTypeHint> getEdgeTypeHints() {
                return List.of(
                        new EdgeTypeHint(UVLModelTypes.MANDATORY,
                                true, true, true,
                                List.of(UVLModelTypes.FEATURE),
                                List.of(UVLModelTypes.FEATURE)),
                        new EdgeTypeHint(UVLModelTypes.OPTIONAL,
                                true, true, true,
                                List.of(UVLModelTypes.FEATURE),
                                List.of(UVLModelTypes.FEATURE)),
                        new EdgeTypeHint(UVLModelTypes.ALTERNATIVE,
                                true, true, true,
                                List.of(UVLModelTypes.FEATURE),
                                List.of(UVLModelTypes.FEATURE)),
                        new EdgeTypeHint(UVLModelTypes.GROUP_CARDINALITY,
                                true, true, true,
                                List.of(UVLModelTypes.FEATURE),
                                List.of(UVLModelTypes.FEATURE)),
                        new EdgeTypeHint(UVLModelTypes.OR,
                                true, true, true,
                                List.of(UVLModelTypes.FEATURE),
                                List.of(UVLModelTypes.FEATURE))
                );
        }
}
