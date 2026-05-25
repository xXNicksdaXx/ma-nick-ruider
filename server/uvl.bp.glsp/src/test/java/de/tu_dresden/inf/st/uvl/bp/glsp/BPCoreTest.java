/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.server.layout.ServerLayoutKind;
import org.junit.jupiter.api.Test;

class BPCoreTest {

  @Test
  void givenDiagramConfiguration_whenCollectingTypeHints_thenIncludesBpShapesAndNoDuplicates() {
    // Arrange
    BPDiagramConfiguration configuration = new BPDiagramConfiguration();

    // Act
    List<String> shapeIds =
        configuration.getShapeTypeHints().stream()
            .map(org.eclipse.glsp.server.types.ShapeTypeHint::getElementTypeId)
            .toList();
    Set<String> shapeIdSet = new HashSet<>(shapeIds);

    // Assert
    assertEquals(ServerLayoutKind.MANUAL, configuration.getLayoutKind());
    assertEquals(shapeIds.size(), shapeIdSet.size());
    assertTrue(
        shapeIdSet.containsAll(
            List.of(
                DefaultTypes.GRAPH,
                UVLModelTypes.FEATURE,
                BPModelTypes.B_THREAD,
                BPModelTypes.REQUESTED_EVENT,
                BPModelTypes.BLOCKED_EVENT,
                BPModelTypes.WAITED_FOR_EVENT)));
  }

  @Test
  void givenDiagramConfiguration_whenCollectingEdgeHints_thenRetainsUvlEdges() {
    // Arrange
    BPDiagramConfiguration configuration = new BPDiagramConfiguration();

    // Act
    List<String> edgeIds =
        configuration.getEdgeTypeHints().stream()
            .map(org.eclipse.glsp.server.types.EdgeTypeHint::getElementTypeId)
            .toList();

    // Assert
    assertTrue(edgeIds.containsAll(List.of(UVLModelTypes.MANDATORY, UVLModelTypes.EXCLUDES)));
  }
}
