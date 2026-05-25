/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.gmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.UVLAttributeFactory;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.GCompartmentBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BPFeatureFactoryTest {

  private TestableBPFeatureFactory factory;
  private UVLModelState modelState;
  private UVLModelIndex modelIndex;
  private UVLAttributeFactory attributeFactory;
  private BPEventFactory bpEventFactory;

  @BeforeEach
  void setUp() {
    factory = new TestableBPFeatureFactory();
    modelState = mock(UVLModelState.class);
    modelIndex = mock(UVLModelIndex.class);
    attributeFactory = mock(UVLAttributeFactory.class);
    bpEventFactory = mock(BPEventFactory.class);

    when(modelState.getIndex()).thenReturn(modelIndex);
    when(modelIndex.getGModelElement(anyString(), eq(GNode.class))).thenReturn(Optional.empty());

    factory.setDependencies(modelState, attributeFactory, bpEventFactory);
  }

  @Test
  void givenBThreadFeatureWithEventAndAttribute_whenCreate_thenBuildsBThreadNodeAndDelegates() {
    // Arrange
    Feature bThread = new Feature("ThreadA");
    Attribute<String> typeAttribute = new Attribute<>("type", "BThread", bThread);
    Attribute<String> priorityAttribute = new Attribute<>("priority", "high", bThread);
    Map<String, Attribute<?>> eventAttrs = new HashMap<>();
    eventAttrs.put("type", new Attribute<>("type", "BEvent", bThread));
    eventAttrs.put("requested", new Attribute<>("requested", true, bThread));
    Attribute<Map<String, Attribute<?>>> eventAttribute =
        new Attribute<>("start", eventAttrs, bThread);

    bThread.getAttributes().put("type", typeAttribute);
    bThread.getAttributes().put("priority", priorityAttribute);
    bThread.getAttributes().put("start", eventAttribute);

    when(modelIndex.getIdFor(bThread)).thenReturn(Optional.of("thread-id"));
    when(attributeFactory.create(priorityAttribute))
        .thenReturn(new GCompartmentBuilder(UVLModelTypes.ATTRIBUTE).id("priority").build());
    when(bpEventFactory.create(eventAttribute))
        .thenReturn(new GCompartmentBuilder(BPModelTypes.REQUESTED_EVENT).id("event-id").build());

    // Act
    GNode node = factory.create(bThread);

    // Assert
    assertEquals(BPModelTypes.B_THREAD, node.getType());
    assertEquals("thread-id", node.getId());
    assertEquals(3, node.getChildren().size());
    verify(attributeFactory, times(1)).create(priorityAttribute);
    verify(attributeFactory, never()).create(typeAttribute);
    verify(attributeFactory, never()).create(eventAttribute);
    verify(bpEventFactory, times(1)).create(eventAttribute);
  }

  @Test
  void givenConfigFeatureWithTypeAttribute_whenCreate_thenHidesTypeAttribute() {
    // Arrange
    Feature config = new Feature("Config");
    Attribute<String> typeAttribute = new Attribute<>("type", "Config", config);
    Attribute<String> modeAttribute = new Attribute<>("mode", "fast", config);
    config.getAttributes().put("type", typeAttribute);
    config.getAttributes().put("mode", modeAttribute);

    when(modelIndex.getIdFor(config)).thenReturn(Optional.of("config-id"));
    when(attributeFactory.create(modeAttribute))
        .thenReturn(new GCompartmentBuilder(UVLModelTypes.ATTRIBUTE).id("mode").build());

    // Act
    GNode node = factory.create(config);

    // Assert
    assertEquals(UVLModelTypes.FEATURE, node.getType());
    assertEquals("config-id", node.getId());
    verify(attributeFactory, times(1)).create(modeAttribute);
    verify(attributeFactory, never()).create(typeAttribute);
    verify(bpEventFactory, never()).create(any());
  }

  @Test
  void givenSubmodelRootBThread_whenCreate_thenAddsSubmodelRootCssClass() {
    // Arrange
    Feature bThread = new Feature("ThreadRoot");
    bThread.setSubmodelRoot(true);
    bThread.getAttributes().put("type", new Attribute<>("type", "BThread", bThread));

    when(modelIndex.getIdFor(bThread)).thenReturn(Optional.of("root-thread-id"));

    // Act
    GNode node = factory.create(bThread);

    // Assert
    assertTrue(node.getCssClasses().contains("submodel-root"));
  }

  private static class TestableBPFeatureFactory extends BPFeatureFactory {
    void setDependencies(
        UVLModelState modelState,
        UVLAttributeFactory attributeFactory,
        BPEventFactory eventFactory) {
      this.modelState = modelState;
      this.attributeFactory = attributeFactory;
      this.bpEventFactory = eventFactory;
    }
  }
}
