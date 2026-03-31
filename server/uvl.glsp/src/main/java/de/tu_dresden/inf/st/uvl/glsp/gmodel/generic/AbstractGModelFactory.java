/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.gmodel.generic;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import java.util.Optional;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.GEdgeBuilder;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;

public abstract class AbstractGModelFactory {

  @Inject protected UVLModelState modelState;

  protected void applyNodeData(GNodeBuilder nodeBuilder, String id) {
    Optional<GNode> node = modelState.getIndex().getGModelElement(id, GNode.class);
    if (node.isPresent()) {
      nodeBuilder.position(node.get().getPosition());
      nodeBuilder.size(node.get().getSize());
    } else {
      // initialize with default values
      nodeBuilder.position(0, 0);
      nodeBuilder.size(64, 32);
    }
  }

  protected void applyEdgeData(GEdgeBuilder edgeBuilder, String id) {
    Optional<GEdge> existingEdge = modelState.getIndex().getGModelElement(id, GEdge.class);
    existingEdge.ifPresent(edge -> edgeBuilder.addRoutingPoints(edge.getRoutingPoints()));
  }
}
