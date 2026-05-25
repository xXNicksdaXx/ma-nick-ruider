/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.gmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.UVLBiConstraintFactory;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.UVLConstraintBoxFactory;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.UVLGroupFactory;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.BPFeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BPGModelFactoriesTest {

  private UVLModelState modelState;

  @BeforeEach
  void setUp() {
    modelState = mock(UVLModelState.class);
    UVLModelIndex modelIndex = mock(UVLModelIndex.class);
    when(modelState.getIndex()).thenReturn(modelIndex);
    when(modelIndex.getGModelElement(anyString(), eq(GNode.class))).thenReturn(Optional.empty());
  }

  @Test
  void givenBpFeatureModelWithContextFeatures_whenFillRootElement_thenAddsOnlyNonContextNodes() {
    // Arrange
    Feature bThread = new Feature("ThreadA");
    bThread.getAttributes().put("type", new Attribute<>("type", "BThread", bThread));
    Feature config = new Feature("Config");
    config.getAttributes().put("type", new Attribute<>("type", "Config", config));
    Feature env = new Feature("Env");
    env.getAttributes().put("type", new Attribute<>("type", "Env", env));

    BPFeatureModel featureModel = new BPFeatureModel();
    featureModel.getFeatureMap().put("thread", bThread);
    featureModel.getFeatureMap().put("config", config);
    featureModel.getFeatureMap().put("env", env);
    featureModel.setConfig(config);
    featureModel.setEnv(env);

    GNode threadNode = new GNodeBuilder(BPModelTypes.B_THREAD).id("thread-node").build();
    GNode envNode = new GNodeBuilder(BPModelTypes.BP_ENV).id("env-node").build();
    GNode configNode = new GNodeBuilder(BPModelTypes.BP_CONFIG).id("config-node").build();

    BPFeatureFactory bpFeatureFactory = mock(BPFeatureFactory.class);
    when(bpFeatureFactory.create(bThread)).thenReturn(threadNode);

    BPAdditionalElementsFactory additionalElementsFactory = mock(BPAdditionalElementsFactory.class);
    when(additionalElementsFactory.createAdditionalElements(featureModel))
        .thenReturn(List.of(envNode, configNode));

    TestableBPGModelFactory factory = new TestableBPGModelFactory();
    factory.setDependencies(
        modelState,
        bpFeatureFactory,
        additionalElementsFactory,
        mock(UVLGroupFactory.class),
        mock(UVLBiConstraintFactory.class),
        mock(UVLConstraintBoxFactory.class));

    GGraph root = new GGraphBuilder(DefaultTypes.GRAPH).id("root").build();

    // Act
    factory.fillRootElementPublic(root, featureModel);

    // Assert
    Set<String> childIds =
        root.getChildren().stream().map(GModelElement::getId).collect(Collectors.toSet());
    assertEquals(Set.of("thread-node", "env-node", "config-node"), childIds);
    verify(bpFeatureFactory, times(1)).create(bThread);
    verify(bpFeatureFactory, never()).create(config);
    verify(bpFeatureFactory, never()).create(env);
    verify(additionalElementsFactory, times(1)).createAdditionalElements(featureModel);
  }

  @Test
  void givenBpFeatureModelWithEnvAndConfig_whenCreateAdditionalElements_thenBuildsContextNodes() {
    // Arrange
    Feature env = new Feature("Env");
    Attribute<String> envType = new Attribute<>("type", "Env", env);
    Attribute<String> envMode = new Attribute<>("mode", "prod", env);
    env.getAttributes().put("type", envType);
    env.getAttributes().put("mode", envMode);

    Feature config = new Feature("Config");
    Attribute<String> configType = new Attribute<>("type", "Config", config);
    Attribute<Boolean> configEnabled = new Attribute<>("enabled", true, config);
    config.getAttributes().put("type", configType);
    config.getAttributes().put("enabled", configEnabled);

    BPFeatureModel featureModel = new BPFeatureModel();
    featureModel.setEnv(env);
    featureModel.setConfig(config);

    TestableBPAdditionalElementsFactory factory = new TestableBPAdditionalElementsFactory();

    // Act
    Collection<GNode> nodes = factory.createAdditionalElements(featureModel);

    // Assert
    Map<String, String> nodeTypes =
        nodes.stream().collect(Collectors.toMap(GNode::getId, GNode::getType));
    assertEquals(
        Map.of("env-id", BPModelTypes.BP_ENV, "config-id", BPModelTypes.BP_CONFIG), nodeTypes);
  }

  private static class TestableBPGModelFactory extends BPGModelFactory {
    void setDependencies(
        UVLModelState modelState,
        BPFeatureFactory bpFeatureFactory,
        BPAdditionalElementsFactory additionalElementsFactory,
        UVLGroupFactory groupFactory,
        UVLBiConstraintFactory biConstraintFactory,
        UVLConstraintBoxFactory constraintBoxFactory) {
      this.modelState = modelState;
      this.bpFeatureFactory = bpFeatureFactory;
      this.additionalElementsFactory = additionalElementsFactory;
      this.groupFactory = groupFactory;
      this.biConstraintFactory = biConstraintFactory;
      this.constraintBoxFactory = constraintBoxFactory;
    }

    void fillRootElementPublic(GGraph root, FeatureModel featureModel) {
      super.fillRootElement(root, featureModel);
    }
  }

  private static class TestableBPAdditionalElementsFactory extends BPAdditionalElementsFactory {
    @Override
    public Collection<GNode> createAdditionalElements(BPFeatureModel featureModel) {
      return List.of(createConfig(featureModel.getConfig()), createEnv(featureModel.getEnv()));
    }

    @Override
    public GNode createConfig(final Feature config) {
      return new GNodeBuilder(BPModelTypes.BP_CONFIG).id("config-id").build();
    }

    @Override
    public GNode createEnv(final Feature env) {
      return new GNodeBuilder(BPModelTypes.BP_ENV).id("env-id").build();
    }
  }
}
