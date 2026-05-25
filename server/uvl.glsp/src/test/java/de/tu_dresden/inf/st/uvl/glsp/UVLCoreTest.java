/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.tu_dresden.inf.st.uvl.glsp.actions.HighlightElementAction;
import de.tu_dresden.inf.st.uvl.glsp.palette.UVLToolPaletteItemProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.server.features.toolpalette.PaletteItem;
import org.eclipse.glsp.server.layout.ServerLayoutKind;
import org.junit.jupiter.api.Test;

class UVLCoreTest {

  @Test
  void
      givenDiagramConfiguration_whenCollectingTypeHints_thenIncludesRequiredTypesAndNoDuplicates() {
    // Arrange
    UVLDiagramConfiguration configuration = new UVLDiagramConfiguration();

    // Act
    List<String> shapeIds =
        configuration.getShapeTypeHints().stream()
            .map(org.eclipse.glsp.server.types.ShapeTypeHint::getElementTypeId)
            .toList();
    List<String> edgeIds =
        configuration.getEdgeTypeHints().stream()
            .map(org.eclipse.glsp.server.types.EdgeTypeHint::getElementTypeId)
            .toList();
    Set<String> shapeIdSet = new HashSet<>(shapeIds);
    Set<String> edgeIdSet = new HashSet<>(edgeIds);

    // Assert
    assertEquals(ServerLayoutKind.MANUAL, configuration.getLayoutKind());
    assertEquals(shapeIds.size(), shapeIdSet.size());
    assertEquals(edgeIds.size(), edgeIdSet.size());
    assertTrue(
        shapeIdSet.containsAll(
            List.of(
                DefaultTypes.GRAPH,
                UVLModelTypes.FEATURE,
                UVLModelTypes.ATTRIBUTE,
                UVLModelTypes.CONSTRAINT_BOX,
                UVLModelTypes.CONSTRAINT)));
    assertTrue(
        edgeIdSet.containsAll(
            List.of(
                UVLModelTypes.MANDATORY,
                UVLModelTypes.OPTIONAL,
                UVLModelTypes.ALTERNATIVE,
                UVLModelTypes.GROUP_CARDINALITY,
                UVLModelTypes.OR,
                UVLModelTypes.REQUIRES,
                UVLModelTypes.EXCLUDES)));
  }

  @Test
  void givenTypeHints_whenReplacingMatchingIds_thenOnlyTargetsReplaced() {
    // Arrange
    UVLDiagramConfiguration configuration = new UVLDiagramConfiguration();
    var original =
        new org.eclipse.glsp.server.types.ShapeTypeHint(
            "custom", false, false, false, false, List.of());
    var replacement =
        new org.eclipse.glsp.server.types.ShapeTypeHint(
            "custom", true, true, true, true, List.of());
    var other =
        new org.eclipse.glsp.server.types.ShapeTypeHint(
            "other", false, false, false, false, List.of());
    List<org.eclipse.glsp.server.types.ShapeTypeHint> hints =
        new java.util.ArrayList<>(List.of(original, other));

    // Act
    configuration.replaceTypeHints(hints, List.of(replacement));

    // Assert
    assertSame(replacement, hints.get(0));
    assertSame(other, hints.get(1));
    assertEquals(2, hints.size());
  }

  @Test
  void givenHighlightAction_whenUpdatingElementIds_thenExposesFirstIdAndHighlightState() {
    // Arrange
    HighlightElementAction action = new HighlightElementAction(List.of("node-1", "node-2"), true);

    // Act
    action.setHighlighted(false);
    action.setElementId("node-3");
    action.setElementIds(List.of("node-4", "node-5"));

    // Assert
    assertEquals(HighlightElementAction.KIND, action.getKind());
    assertFalse(action.isHighlighted());
    assertEquals(List.of("node-4", "node-5"), action.getElementIds());
    assertEquals("node-4", action.getElementId());
  }

  @Test
  void givenEmptyElementIds_whenReadingFirstElement_thenReturnsNull() {
    // Arrange
    HighlightElementAction action = new HighlightElementAction();
    action.setElementIds(null);

    // Act
    String elementId = action.getElementId();

    // Assert
    assertNull(elementId);
    assertEquals(HighlightElementAction.KIND, action.getKind());
  }

  @Test
  void givenPaletteProvider_whenBuildingGroups_thenCreatesElementsRelationsConstraints() {
    // Arrange
    UVLToolPaletteItemProvider provider = new UVLToolPaletteItemProvider();

    // Act
    List<PaletteItem> groups = provider.getItems(Map.of());

    // Assert
    assertEquals(3, groups.size());
    Set<String> groupIds = groups.stream().map(PaletteItem::getId).collect(Collectors.toSet());
    assertEquals(Set.of("elements", "relations", "constraints"), groupIds);
    assertEquals("Elements", groups.get(0).getLabel());
    assertEquals("Relations", groups.get(1).getLabel());
    assertEquals("Constraints", groups.get(2).getLabel());
  }
}
