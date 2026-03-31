/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.gmodel;

import static de.tu_dresden.inf.st.uvl.glsp.utils.ConstraintUtil.convertConstraintTypeToModelType;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.generic.AbstractSingleGModelFactory;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.metamodel.model.building.VariableReference;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.LiteralConstraint;
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

    String sourceId;
    String targetId;

    Constraint sourceConstraint = constraint.getConstraintSubParts().getFirst();
    if (sourceConstraint instanceof LiteralConstraint sourceLiteralConstraint) {
      VariableReference sourceReference = sourceLiteralConstraint.getReference();
      sourceId =
          index
              .getIdFor(sourceReference)
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Source feature of implication constraint not indexed: "
                              + sourceReference.getIdentifier()));
    } else {
      throw new IllegalStateException(
          "Unsupported source constraint type for implication: "
              + sourceConstraint.getClass().getName());
    }

    Constraint targetReference = constraint.getConstraintSubParts().getLast();
    if (targetReference instanceof LiteralConstraint targetLiteralConstraint) {
      VariableReference targetFeature = targetLiteralConstraint.getReference();
      targetId =
          index
              .getIdFor(targetFeature)
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Target feature of implication constraint not indexed: "
                              + targetFeature.getIdentifier()));
    } else {
      throw new IllegalStateException(
          "Unsupported target constraint type for implication: "
              + targetReference.getClass().getName());
    }

    String type = convertConstraintTypeToModelType(constraint);

    GEdgeBuilder edgeBuilder =
        new GEdgeBuilder(type)
            .id(id)
            .sourceId(sourceId)
            .targetId(targetId)
            .routerKind(GConstants.RouterKind.POLYLINE)
            .addCssClass("constraint-edge")
            .add(
                new GLabelBuilder(UVLModelTypes.CONSTRAINT_LABEL)
                    .id(id + "_label")
                    .text("requires")
                    .addCssClass("edge-label")
                    .edgePlacement(
                        new GEdgePlacementBuilder()
                            .side(GConstants.EdgeSide.TOP)
                            .position(0.5d)
                            .offset(1.5d)
                            .rotate(false)
                            .build())
                    .build());

    applyEdgeData(edgeBuilder, id);
    return edgeBuilder.build();
  }
}
