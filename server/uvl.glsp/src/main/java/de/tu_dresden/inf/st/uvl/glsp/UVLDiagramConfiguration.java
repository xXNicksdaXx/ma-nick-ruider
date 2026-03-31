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
import org.eclipse.glsp.server.types.ElementTypeHint;
import org.eclipse.glsp.server.types.ShapeTypeHint;

public class UVLDiagramConfiguration extends BaseDiagramConfiguration {

  @Override
  public List<ShapeTypeHint> getShapeTypeHints() {
    return List.of(
        new ShapeTypeHint(
            DefaultTypes.GRAPH, false, false, false, false, List.of(UVLModelTypes.FEATURE)),
        new ShapeTypeHint(
            UVLModelTypes.FEATURE,
            true,
            true,
            false,
            true,
            List.of(
                UVLModelTypes.FEATURE, UVLModelTypes.ATTRIBUTE, UVLModelTypes.CARDINALITY_LABEL)),
        new ShapeTypeHint(
            UVLModelTypes.ATTRIBUTE, false, true, false, false, List.of(UVLModelTypes.ATTRIBUTE)),
        new ShapeTypeHint(
            UVLModelTypes.CONSTRAINT_BOX,
            true,
            false,
            false,
            false,
            List.of(UVLModelTypes.CONSTRAINT)),
        new ShapeTypeHint(UVLModelTypes.CONSTRAINT, false, true, false, true));
  }

  @Override
  public List<EdgeTypeHint> getEdgeTypeHints() {
    return List.of(
        new EdgeTypeHint(
            UVLModelTypes.MANDATORY,
            false,
            false,
            false,
            List.of(UVLModelTypes.FEATURE),
            List.of(UVLModelTypes.FEATURE)),
        new EdgeTypeHint(
            UVLModelTypes.OPTIONAL,
            false,
            false,
            false,
            List.of(UVLModelTypes.FEATURE),
            List.of(UVLModelTypes.FEATURE)),
        new EdgeTypeHint(
            UVLModelTypes.ALTERNATIVE,
            false,
            false,
            false,
            List.of(UVLModelTypes.FEATURE),
            List.of(UVLModelTypes.FEATURE)),
        new EdgeTypeHint(
            UVLModelTypes.GROUP_CARDINALITY,
            false,
            false,
            false,
            List.of(UVLModelTypes.FEATURE),
            List.of(UVLModelTypes.FEATURE)),
        new EdgeTypeHint(
            UVLModelTypes.OR,
            false,
            false,
            false,
            List.of(UVLModelTypes.FEATURE),
            List.of(UVLModelTypes.FEATURE)),
        new EdgeTypeHint(
            UVLModelTypes.IMPLICATION,
            false,
            true,
            true,
            List.of(UVLModelTypes.FEATURE),
            List.of(UVLModelTypes.FEATURE)),
        new EdgeTypeHint(
            UVLModelTypes.EQUIVALENCE,
            false,
            true,
            true,
            List.of(UVLModelTypes.FEATURE),
            List.of(UVLModelTypes.FEATURE)));
  }

  protected <T extends ElementTypeHint> void replaceTypeHints(
      final List<T> typeHints, final List<T> newTypeHints) {
    for (T newTypeHint : newTypeHints) {
      typeHints.replaceAll(
          typeHint ->
              typeHint.getElementTypeId().equals(newTypeHint.getElementTypeId())
                  ? newTypeHint
                  : typeHint);
    }
  }
}
