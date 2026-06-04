/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.gmodel;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.generic.AbstractCollectedGModelFactory;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil;
import de.tu_dresden.inf.st.uvl.glsp.utils.GroupUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.builder.impl.GEdgeBuilder;
import org.eclipse.glsp.graph.builder.impl.GEdgePlacementBuilder;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.util.GConstants;

public class UVLGroupFactory extends AbstractCollectedGModelFactory<Group, GEdge> {

  @Override
  public Collection<GEdge> create(final Group group) {
    UVLModelIndex index = modelState.getIndex();
    List<GEdge> edges = new ArrayList<>();

    String groupId =
        index
            .getIdFor(group)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Group not indexed: " + GroupUtil.getGroupName(group)));
    String sourceId =
        index
            .getIdFor(group.getParentFeature())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Parent feature of group not indexed: "
                            + group.getParentFeature().getFeatureName()));

    String type;
    switch (group.GROUPTYPE) {
      case MANDATORY -> type = UVLModelTypes.MANDATORY;
      case OPTIONAL -> type = UVLModelTypes.OPTIONAL;
      case ALTERNATIVE -> type = UVLModelTypes.ALTERNATIVE;
      case GROUP_CARDINALITY -> type = UVLModelTypes.GROUP_CARDINALITY;
      case OR -> type = UVLModelTypes.OR;
      default -> throw new IllegalArgumentException("Unknown group type: " + group.GROUPTYPE);
    }

    boolean requiresCardinalityLabel = group.GROUPTYPE == Group.GroupType.GROUP_CARDINALITY;

    for (Feature target : group.getFeatures()) {
      String targetId =
          index
              .getIdFor(target)
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Target feature of group not indexed: " + target.getFeatureName()));

      GEdgeBuilder edgeBuilder =
          new GEdgeBuilder(type)
              .id(groupId + "_" + targetId)
              .addCssClass("centered-anchor")
              .addCssClass(group.GROUPTYPE.name().toLowerCase())
              .sourceId(sourceId)
              .targetId(targetId)
              .routerKind(GConstants.RouterKind.POLYLINE);

      if (requiresCardinalityLabel) {
        edgeBuilder.add(
            new GLabelBuilder(UVLModelTypes.CARDINALITY_LABEL)
                .id(groupId + "_" + targetId + "_label")
                .text(FeatureModelUtil.getCardinalityText(group.getCardinality()))
                .addCssClass("edge-label")
                .edgePlacement(
                    new GEdgePlacementBuilder()
                        .side(GConstants.EdgeSide.LEFT)
                        .position(0.2d)
                        .offset(-8d)
                        .rotate(false)
                        .build())
                .build());
        requiresCardinalityLabel = false;
      }

      edges.add(edgeBuilder.build());
    }

    return edges;
  }
}
