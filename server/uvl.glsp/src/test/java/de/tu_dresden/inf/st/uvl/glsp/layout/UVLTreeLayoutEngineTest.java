/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group.GroupType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.eclipse.glsp.graph.util.GraphUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UVLTreeLayoutEngineTest {

  private UVLTreeLayoutEngine layoutEngine;
  private UVLModelIndex modelIndex;

  @BeforeEach
  void setUp() throws Exception {
    layoutEngine = new UVLTreeLayoutEngine();
    UVLModelState modelState = mock(UVLModelState.class);
    modelIndex = mock(UVLModelIndex.class);

    Field modelStateField = UVLTreeLayoutEngine.class.getDeclaredField("modelState");
    modelStateField.setAccessible(true);
    modelStateField.set(layoutEngine, modelState);

    when(modelState.getIndex()).thenReturn(modelIndex);
  }

  @Test
  void submodelRootFeatureIsTreatedAsLeafDuringTreeTraversal() throws Exception {
    Feature root = new Feature("Root");
    Feature normalChild = new Feature("NormalChild");
    Feature normalGrandChild = new Feature("NormalGrandChild");
    Feature submodelRoot = new Feature("SubmodelRoot");
    submodelRoot.setSubmodelRoot(true);
    Feature hiddenChild = new Feature("HiddenChild");

    Group rootGroup = new Group(GroupType.MANDATORY);
    Group normalChildGroup = new Group(GroupType.MANDATORY);
    Group submodelGroup = new Group(GroupType.MANDATORY);

    root.getChildren().add(rootGroup);
    rootGroup.getFeatures().add(normalChild);
    rootGroup.getFeatures().add(submodelRoot);

    normalChild.getChildren().add(normalChildGroup);
    normalChildGroup.getFeatures().add(normalGrandChild);

    submodelRoot.getChildren().add(submodelGroup);
    submodelGroup.getFeatures().add(hiddenChild);

    stubFeature(root, "root_id");
    stubFeature(normalChild, "normal_child_id");
    stubFeature(normalGrandChild, "normal_grand_child_id");
    stubFeature(submodelRoot, "submodel_root_id");
    stubFeature(hiddenChild, "hidden_child_id");

    WalkersNode rootNode = invokeTransformFeature(root);

    assertEquals(
        2, rootNode.children.size(), "Root should still traverse into both direct children");

    WalkersNode normalChildNode = findChild(rootNode, "normal_child_id");
    assertNotNull(normalChildNode, "Normal child should be present in the tree");
    assertEquals(
        1, normalChildNode.children.size(), "Normal child should still traverse into its children");

    WalkersNode submodelRootNode = findChild(rootNode, "submodel_root_id");
    assertNotNull(submodelRootNode, "Submodel root should be present in the tree");
    assertTrue(submodelRootNode.children.isEmpty(), "Submodel roots must be treated as leaf nodes");
  }

  @Test
  void givenVariableHeights_whenLayout_thenRespectsMaxHeightOffsets() {
    // Arrange
    WalkersNode root = new WalkersNode("root", "Root", 80, 100);
    WalkersNode child = new WalkersNode("child", "Child", 60, 20);
    root.children.add(child);

    // Act
    layoutEngine.layout(root);

    // Assert
    double expectedChildY = 100.0 + 8.0 + 80.0;
    assertEquals(0.0, root.y, 0.001);
    assertEquals(expectedChildY, child.y, 0.001);
    assertTrue(root.x >= 0, "Root should be normalized to a non-negative x");
    assertTrue(child.x >= 0, "Child should be normalized to a non-negative x");
  }

  @Test
  void givenOverlappingSiblings_whenLayout_thenSeparatesBySiblingGap() {
    // Arrange
    WalkersNode root = new WalkersNode("root", "Root", 40, 40);
    WalkersNode left = new WalkersNode("left", "Left", 120, 30);
    WalkersNode right = new WalkersNode("right", "Right", 120, 30);
    root.children.add(left);
    root.children.add(right);

    // Act
    layoutEngine.layout(root);

    // Assert
    List<WalkersNode> ordered =
        Stream.of(left, right).sorted(Comparator.comparingDouble(node -> node.x)).toList();
    double minSpacing = (ordered.get(0).width / 2.0) + (ordered.get(1).width / 2.0) + 20.0;
    double actualSpacing = ordered.get(1).x - ordered.get(0).x;
    assertTrue(actualSpacing >= minSpacing - 0.001, "Siblings should not overlap");
  }

  private void stubFeature(Feature feature, String id) {
    when(modelIndex.getIdFor(feature)).thenReturn(Optional.of(id));
    when(modelIndex.getGModelElement(eq(id), eq(GNode.class))).thenReturn(Optional.of(mockNode()));
  }

  private GNode mockNode() {
    GNode node = new GNodeBuilder("test").id("test").build();
    node.setSize(GraphUtil.dimension(64, 32));
    return node;
  }

  private WalkersNode invokeTransformFeature(Feature feature) throws Exception {
    Method method =
        UVLTreeLayoutEngine.class.getDeclaredMethod(
            "transformFeature", Feature.class, UVLModelIndex.class);
    method.setAccessible(true);
    return (WalkersNode) method.invoke(layoutEngine, feature, modelIndex);
  }

  private WalkersNode findChild(WalkersNode parent, String id) {
    return parent.children.stream().filter(child -> id.equals(child.id)).findFirst().orElse(null);
  }
}
