/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.gmodel;

import static de.tu_dresden.inf.st.uvl.glsp.utils.ConstraintUtil.convertConstraintTypeToModelType;
import static de.tu_dresden.inf.st.uvl.glsp.utils.ConstraintUtil.getBiConstraintSource;
import static de.tu_dresden.inf.st.uvl.glsp.utils.ConstraintUtil.getBiConstraintTarget;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.generic.AbstractSingleGModelFactory;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.builder.impl.GEdgeBuilder;
import org.eclipse.glsp.graph.builder.impl.GEdgePlacementBuilder;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.util.GConstants;

public class UVLBiConstraintFactory extends AbstractSingleGModelFactory<Constraint, GEdge> {

  @Override
  public GEdge create(final Constraint constraint) {
    UVLModelIndex index = modelState.getIndex();

    String id =
        index
            .getIdFor(constraint)
            .orElseThrow(
                () ->
                    new IllegalStateException("Constraint not indexed: " + constraint.toString()));
    String type = convertConstraintTypeToModelType(constraint);

    if (UVLModelTypes.REQUIRES.equals(type)) {
      return createRequiresEdge(constraint, index, id);
    } else if (UVLModelTypes.EXCLUDES.equals(type)) {
      return createExcludesEdge(constraint, index, id);
    }

    throw new IllegalStateException("Unsupported bi-constraint type for: " + constraint);
  }

  protected GEdge createRequiresEdge(
      final Constraint constraint, final UVLModelIndex index, final String id) {
    String sourceId =
        index
            .getIdFor(getBiConstraintSource(constraint).getReference())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Source feature of requires constraint not indexed for: " + constraint));

    String targetId =
        index
            .getIdFor(getBiConstraintTarget(constraint).getReference())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Target feature of requires constraint not indexed for: " + constraint));

    GEdgeBuilder edgeBuilder =
        new GEdgeBuilder(UVLModelTypes.REQUIRES)
            .id(id)
            .sourceId(sourceId)
            .targetId(targetId)
            .routerKind(GConstants.RouterKind.POLYLINE)
            .addCssClass("constraint-edge")
            .add(
                new GLabelBuilder(UVLModelTypes.CONSTRAINT_EDGE_LABEL)
                    .id(id + "_label")
                    .text("requires")
                    .addCssClass("edge-label")
                    .edgePlacement(
                        new GEdgePlacementBuilder()
                            .side(GConstants.EdgeSide.TOP)
                            .position(0.5d)
                            .offset(8d)
                            .rotate(true)
                            .build())
                    .build());

    applyEdgeData(edgeBuilder, id);
    return edgeBuilder.build();
  }

  protected GEdge createExcludesEdge(
      final Constraint constraint, final UVLModelIndex index, final String id) {
    String sourceId =
        index
            .getIdFor(getBiConstraintSource(constraint).getReference())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Source feature of excludes constraint not indexed for: " + constraint));

    String targetId =
        index
            .getIdFor(getBiConstraintTarget(constraint).getReference())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Target feature of excludes constraint not indexed for: " + constraint));

    GEdgeBuilder edgeBuilder =
        new GEdgeBuilder(UVLModelTypes.EXCLUDES)
            .id(id)
            .sourceId(sourceId)
            .targetId(targetId)
            .routerKind(GConstants.RouterKind.POLYLINE)
            .addCssClass("constraint-edge")
            .add(
                new GLabelBuilder(UVLModelTypes.CONSTRAINT_EDGE_LABEL)
                    .id(id + "_label")
                    .text("excludes")
                    .addCssClass("edge-label")
                    .edgePlacement(
                        new GEdgePlacementBuilder()
                            .side(GConstants.EdgeSide.TOP)
                            .position(0.5d)
                            .offset(8d)
                            .rotate(true)
                            .build())
                    .build());

    applyEdgeData(edgeBuilder, id);
    return edgeBuilder.build();
  }
}
