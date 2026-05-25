/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import java.util.Optional;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.GPoint;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;
import org.eclipse.glsp.graph.util.GraphUtil;
import org.eclipse.glsp.server.operations.CreateNodeOperation;
import org.junit.jupiter.api.Test;

class UVLCreateFeatureOperationHandlerTest {

  @Test
  void givenMissingRootFeature_whenCreateRootFeature_thenInitializesModelAndSelectsNode() {
    // Arrange
    TestableCreateFeatureHandler handler = new TestableCreateFeatureHandler();
    UVLModelState modelState = mock(UVLModelState.class);
    FeatureModel featureModel = new FeatureModel();
    GGraph root = new GGraphBuilder(DefaultTypes.GRAPH).id("root").build();
    CreateNodeOperation operation = mock(CreateNodeOperation.class);
    when(operation.getLocation()).thenReturn(Optional.of(GraphUtil.point(10, 20)));
    when(modelState.getRoot()).thenReturn(root);
    when(modelState.getFeatureModel()).thenReturn(featureModel);
    handler.modelState = modelState;

    // Act
    handler.exposeCreateRootFeature(operation);

    // Assert
    assertEquals(1, root.getChildren().size());
    GNode node = (GNode) root.getChildren().getFirst();
    assertEquals(10.0, node.getPosition().getX(), 0.001);
    assertEquals(20.0, node.getPosition().getY(), 0.001);
    assertEquals("Feature1", featureModel.getRootFeature().getFeatureName());
    assertTrue(featureModel.getFeatureMap().containsKey("Feature1"));
    assertEquals("Feature1", ((GLabel) node.getChildren().getFirst()).getText());
    assertEquals(node.getId(), handler.getSelectedId());
    verify(modelState).setFeatureModel(featureModel);
    verify(modelState).updateIndex();
  }

  @Test
  void givenContainerFeatureId_whenGetInitialFeatureLink_thenReturnsContainerFeature() {
    // Arrange
    TestableCreateFeatureHandler handler = new TestableCreateFeatureHandler();
    UVLModelState modelState = mock(UVLModelState.class);
    UVLModelIndex modelIndex = mock(UVLModelIndex.class);
    Feature parent = new Feature("Parent");
    when(modelState.getIndex()).thenReturn(modelIndex);
    when(modelIndex.getUVLObject("container-id", Feature.class)).thenReturn(Optional.of(parent));
    handler.modelState = modelState;

    // Act
    Feature resolved = handler.exposeGetInitialFeatureLink("container-id");

    // Assert
    assertSame(parent, resolved);
  }

  @Test
  void givenMissingContainerFeatureId_whenGetInitialFeatureLink_thenFallsBackToRootFeature() {
    // Arrange
    TestableCreateFeatureHandler handler = new TestableCreateFeatureHandler();
    UVLModelState modelState = mock(UVLModelState.class);
    UVLModelIndex modelIndex = mock(UVLModelIndex.class);
    Feature rootFeature = new Feature("Root");
    FeatureModel featureModel = new FeatureModel();
    featureModel.setRootFeature(rootFeature);
    when(modelState.getFeatureModel()).thenReturn(featureModel);
    when(modelState.getIndex()).thenReturn(modelIndex);
    when(modelIndex.getUVLObject("missing-id", Feature.class)).thenReturn(Optional.empty());
    when(modelIndex.getIdFor(rootFeature)).thenReturn(Optional.of("root-id"));
    when(modelIndex.getUVLObject("root-id", Feature.class)).thenReturn(Optional.of(rootFeature));
    handler.modelState = modelState;

    // Act
    Feature resolved = handler.exposeGetInitialFeatureLink("missing-id");

    // Assert
    assertSame(rootFeature, resolved);
  }

  @Test
  void givenParentPosition_whenCalculateNewFeatureLocation_thenUsesSpacingAndOffsetRange() {
    // Arrange
    TestableCreateFeatureHandler handler = new TestableCreateFeatureHandler();
    GPoint parentPosition = GraphUtil.point(50, 100);

    // Act
    GPoint newLocation = handler.exposeCalculateNewFeatureLocation(parentPosition);

    // Assert
    assertEquals(180.0, newLocation.getY(), 0.001);
    assertTrue(newLocation.getX() >= -78.0 && newLocation.getX() <= 178.0);
  }

  @Test
  void givenLocation_whenCreateGNode_thenUsesProvidedCoordinates() {
    // Arrange
    TestableCreateFeatureHandler handler = new TestableCreateFeatureHandler();
    handler.modelState = mock(UVLModelState.class);
    when(handler.modelState.getFeatureModel()).thenReturn(new FeatureModel());

    // Act
    GNode node = handler.exposeCreateGNode(GraphUtil.point(5, 6));

    // Assert
    assertEquals(5.0, node.getPosition().getX(), 0.001);
    assertEquals(6.0, node.getPosition().getY(), 0.001);
  }

  private static final class TestableCreateFeatureHandler extends UVLCreateFeatureOperationHandler {
    private String selectedId;

    @Override
    protected void selectElement(GNode node) {
      selectedId = node.getId();
    }

    String getSelectedId() {
      return selectedId;
    }

    void exposeCreateRootFeature(CreateNodeOperation operation) {
      createRootFeature(operation);
    }

    Feature exposeGetInitialFeatureLink(String containerId) {
      return getInitialFeatureLink(containerId);
    }

    GNode exposeCreateGNode(GPoint location) {
      return createGNode(location);
    }

    GPoint exposeCalculateNewFeatureLocation(GPoint parentPosition) {
      return calculateNewFeatureLocation(parentPosition);
    }
  }
}
