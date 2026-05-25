/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.gmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Cardinality;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group.GroupType;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.ImplicationConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.LiteralConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.NotConstraint;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GCompartment;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.GCompartmentBuilder;
import org.eclipse.glsp.graph.builder.impl.GEdgeBuilder;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UVLGModelFactoriesTest {

  private UVLModelState modelState;
  private UVLModelIndex modelIndex;

  @BeforeEach
  void setUp() {
    modelState = mock(UVLModelState.class);
    modelIndex = mock(UVLModelIndex.class);
    when(modelState.getIndex()).thenReturn(modelIndex);
  }

  @Test
  void attributeFactoryCreatesLeafAndNestedAttributes() throws Exception {
    UVLAttributeFactory factory = new UVLAttributeFactory();
    inject(factory, "modelState", modelState);

    Feature parent = new Feature("Parent");
    when(modelIndex.getIdFor(parent)).thenReturn(Optional.of("feature-id"));

    Attribute<Integer> leaf = new Attribute<>("count", 7, parent);
    GCompartment leafCompartment = factory.create(leaf);
    assertEquals(UVLModelTypes.ATTRIBUTE, leafCompartment.getType());
    assertEquals("feature-id_attribute[count]", leafCompartment.getId());
    assertEquals(4, leafCompartment.getChildren().size());
    assertEquals("count", ((GLabel) leafCompartment.getChildren().get(1)).getText());
    assertEquals("7", ((GLabel) leafCompartment.getChildren().get(3)).getText());

    Map<String, Attribute<?>> nestedAttributes = new java.util.LinkedHashMap<>();
    nestedAttributes.put("inner", new Attribute<>("inner", true, parent));
    Attribute<Map<String, Attribute<?>>> nested =
        new Attribute<>("settings", nestedAttributes, parent);

    GCompartment nestedCompartment = factory.create(nested);
    assertEquals(UVLModelTypes.ATTRIBUTE, nestedCompartment.getType());
    assertEquals("feature-id_attribute[settings]", nestedCompartment.getId());
    assertEquals(3, nestedCompartment.getChildren().size());
    GCompartment openCompartment = (GCompartment) nestedCompartment.getChildren().getFirst();
    assertEquals("settings", ((GLabel) openCompartment.getChildren().get(1)).getText());
  }

  @Test
  void featureFactoryCreatesFeatureNodesWithCardinalityAndAttributes() throws Exception {
    UVLFeatureFactory factory = new UVLFeatureFactory();
    inject(factory, "modelState", modelState);
    UVLAttributeFactory attributeFactory = mock(UVLAttributeFactory.class);
    inject(factory, "attributeFactory", attributeFactory);

    Feature feature = new Feature("FeatureA");
    feature.setCardinality(new Cardinality(0, 2));
    feature.setSubmodelRoot(true);
    feature.getAttributes().put("count", new Attribute<>("count", 1, feature));
    when(modelIndex.getIdFor(feature)).thenReturn(Optional.of("feature-id"));

    when(attributeFactory.create(any()))
        .thenAnswer(
            invocation -> {
              Attribute<?> attribute = invocation.getArgument(0);
              return new GCompartmentBuilder(UVLModelTypes.ATTRIBUTE)
                  .id(attribute.getName() + "-attribute")
                  .build();
            });

    GNode node = factory.create(feature);
    assertEquals(UVLModelTypes.FEATURE, node.getType());
    assertEquals("feature-id", node.getId());
    assertTrue(node.getCssClasses().contains("submodel-root"));
    assertEquals(2, node.getChildren().size());

    GCompartment header = (GCompartment) node.getChildren().getFirst();
    assertEquals(DefaultTypes.COMPARTMENT_HEADER, header.getType());
    assertEquals(4, header.getChildren().size());
    assertEquals("FeatureA", ((GLabel) header.getChildren().get(0)).getText());
    assertEquals(" [", ((GLabel) header.getChildren().get(1)).getText());
    assertEquals("0..2", ((GLabel) header.getChildren().get(2)).getText());
    assertEquals("]", ((GLabel) header.getChildren().get(3)).getText());
  }

  @Test
  void groupFactoryCreatesEdgesAndCardinalityLabelsForTheFirstTargetOnly() throws Exception {
    UVLGroupFactory factory = new UVLGroupFactory();
    inject(factory, "modelState", modelState);

    Feature source = new Feature("Source");
    Feature left = new Feature("Left");
    Feature right = new Feature("Right");
    Group group = new Group(GroupType.GROUP_CARDINALITY);
    group.setParentFeature(source);
    group.setCardinality(new Cardinality(1, 3));
    group.getFeatures().add(left);
    group.getFeatures().add(right);

    when(modelIndex.getIdFor(group)).thenReturn(Optional.of("group-id"));
    when(modelIndex.getIdFor(source)).thenReturn(Optional.of("source-id"));
    when(modelIndex.getIdFor(left)).thenReturn(Optional.of("left-id"));
    when(modelIndex.getIdFor(right)).thenReturn(Optional.of("right-id"));

    Collection<GEdge> edges = factory.create(group);
    assertEquals(2, edges.size());

    GEdge first = edges.stream().findFirst().orElseThrow();
    assertEquals(UVLModelTypes.GROUP_CARDINALITY, first.getType());
    assertEquals("group-id_left-id", first.getId());
    assertEquals("source-id", first.getSourceId());
    assertEquals("left-id", first.getTargetId());
    assertEquals(1, first.getChildren().size());
    assertEquals(UVLModelTypes.CARDINALITY_LABEL, first.getChildren().getFirst().getType());
    assertEquals("1..3", ((GLabel) first.getChildren().getFirst()).getText());

    GEdge second = edges.stream().skip(1).findFirst().orElseThrow();
    assertEquals(0, second.getChildren().size());
    assertEquals("group-id_right-id", second.getId());
  }

  @Test
  void constraintBoxFactoryCreatesConstraintCompartments() throws Exception {
    UVLConstraintBoxFactory factory = new UVLConstraintBoxFactory();
    inject(factory, "modelState", modelState);

    Feature root = new Feature("Root");
    FeatureModel featureModel = new FeatureModel();
    featureModel.setRootFeature(root);
    LiteralConstraint complexConstraint = new LiteralConstraint(root);
    featureModel.getOwnConstraints().add(complexConstraint);

    when(modelIndex.getIdFor(complexConstraint)).thenReturn(Optional.of("constraint-id"));

    GNode box = factory.create(featureModel);
    assertEquals(UVLModelTypes.CONSTRAINT_BOX, box.getType());
    assertEquals("constraint_box", box.getId());
    assertEquals(2, box.getChildren().size());

    GCompartment compartment = (GCompartment) box.getChildren().get(1);
    assertEquals(DefaultTypes.COMPARTMENT, compartment.getType());
    assertEquals(1, compartment.getChildren().size());
    GLabel constraintLabel =
        compartment.getChildren().stream()
            .flatMap(child -> child.getChildren().stream())
            .filter(GLabel.class::isInstance)
            .map(GLabel.class::cast)
            .findFirst()
            .orElseThrow();
    assertEquals("Root", constraintLabel.getText());
  }

  @Test
  void biConstraintFactoryCreatesRequiresAndExcludesEdges() throws Exception {
    UVLBiConstraintFactory factory = new UVLBiConstraintFactory();
    inject(factory, "modelState", modelState);

    Feature left = new Feature("Left");
    Feature right = new Feature("Right");
    LiteralConstraint leftLiteral = new LiteralConstraint(left);
    LiteralConstraint rightLiteral = new LiteralConstraint(right);
    ImplicationConstraint requires = new ImplicationConstraint(leftLiteral, rightLiteral);
    NotConstraint excludes =
        new NotConstraint(
            new de.tu_dresden.inf.st.uvl.metamodel.model.constraint.AndConstraint(
                leftLiteral, rightLiteral));

    when(modelIndex.getIdFor(requires)).thenReturn(Optional.of("requires-id"));
    when(modelIndex.getIdFor(excludes)).thenReturn(Optional.of("excludes-id"));
    when(modelIndex.getIdFor(left)).thenReturn(Optional.of("left-id"));
    when(modelIndex.getIdFor(right)).thenReturn(Optional.of("right-id"));

    GEdge requiresEdge = factory.create(requires);
    assertEquals(UVLModelTypes.REQUIRES, requiresEdge.getType());
    assertEquals("requires-id", requiresEdge.getId());
    assertEquals("left-id", requiresEdge.getSourceId());
    assertEquals("right-id", requiresEdge.getTargetId());
    assertEquals("requires", ((GLabel) requiresEdge.getChildren().getFirst()).getText());

    GEdge excludesEdge = factory.create(excludes);
    assertEquals(UVLModelTypes.EXCLUDES, excludesEdge.getType());
    assertEquals("excludes", ((GLabel) excludesEdge.getChildren().getFirst()).getText());
  }

  @Test
  void gModelFactoryPlacesVisibleElementsAndConstraintBoxesIntoTheRoot() throws Exception {
    UVLGModelFactory factory = new UVLGModelFactory();
    inject(factory, "modelState", modelState);
    UVLFeatureFactory featureFactory = mock(UVLFeatureFactory.class);
    UVLGroupFactory groupFactory = mock(UVLGroupFactory.class);
    UVLBiConstraintFactory biConstraintFactory = mock(UVLBiConstraintFactory.class);
    UVLConstraintBoxFactory constraintBoxFactory = mock(UVLConstraintBoxFactory.class);
    inject(factory, "featureFactory", featureFactory);
    inject(factory, "groupFactory", groupFactory);
    inject(factory, "biConstraintFactory", biConstraintFactory);
    inject(factory, "constraintBoxFactory", constraintBoxFactory);

    Feature rootFeature = new Feature("Root");
    Feature childFeature = new Feature("Child");
    Group group = new Group(GroupType.MANDATORY);
    group.setParentFeature(rootFeature);
    group.getFeatures().add(childFeature);
    rootFeature.getChildren().add(group);

    FeatureModel featureModel = new FeatureModel();
    featureModel.setRootFeature(rootFeature);
    featureModel.getFeatureMap().put(rootFeature.getFeatureName(), rootFeature);
    featureModel.getFeatureMap().put(childFeature.getFeatureName(), childFeature);
    LiteralConstraint rootConstraint = new LiteralConstraint(rootFeature);
    featureModel.getOwnConstraints().add(rootConstraint);

    when(modelState.getFeatureModel()).thenReturn(featureModel);
    when(modelState.getClientOptions())
        .thenReturn(Map.of("sourceUri", "file:///C:/temp/model.uvl"));
    GGraph existingRoot = new GGraphBuilder(DefaultTypes.GRAPH).id("old-root").revision(7).build();
    when(modelState.getRoot()).thenReturn(existingRoot);

    when(modelIndex.getIdFor(rootFeature)).thenReturn(Optional.of("root-id"));
    when(modelIndex.getIdFor(childFeature)).thenReturn(Optional.of("child-id"));
    when(modelIndex.getIdFor(group)).thenReturn(Optional.of("group-id"));
    when(modelIndex.getIdFor(rootConstraint)).thenReturn(Optional.of("constraint-id"));
    when(modelIndex.getIdFor(rootFeature)).thenReturn(Optional.of("root-id"));

    inject(factory.featureFactory, "attributeFactory", new UVLAttributeFactory());
    inject(factory.featureFactory.attributeFactory, "modelState", modelState);
    inject(factory.featureFactory, "modelState", modelState);

    when(featureFactory.create(any()))
        .thenAnswer(
            invocation -> {
              Feature feature = invocation.getArgument(0);
              return new GNodeBuilder(UVLModelTypes.FEATURE)
                  .id(feature.getFeatureName() + "-node")
                  .build();
            });
    when(groupFactory.create(any()))
        .thenAnswer(
            invocation -> {
              Group createdGroup = invocation.getArgument(0);
              return List.of(
                  new GEdgeBuilder(UVLModelTypes.MANDATORY)
                      .id(createdGroup.getParentFeature().getFeatureName() + "-edge")
                      .sourceId("root-id")
                      .targetId("child-id")
                      .build());
            });
    when(biConstraintFactory.create(any()))
        .thenReturn(
            new GEdgeBuilder(UVLModelTypes.REQUIRES)
                .id("constraint-edge")
                .sourceId("root-id")
                .targetId("child-id")
                .build());
    when(constraintBoxFactory.create(any()))
        .thenReturn(new GNodeBuilder(UVLModelTypes.CONSTRAINT_BOX).id("constraint_box").build());

    GGraph root = factory.createRootElement();
    assertEquals("file:///C:/temp/model.uvl", root.getId());
    assertEquals(7, root.getRevision());

    factory.fillRootElement(root, featureModel);
    assertFalse(root.getChildren().isEmpty());
    assertEquals(4, root.getChildren().size());
    assertEquals("Root-node", root.getChildren().get(0).getId());
    assertEquals("Child-node", root.getChildren().get(1).getId());
    assertEquals("Root-edge", root.getChildren().get(2).getId());
    assertEquals("constraint_box", root.getChildren().get(3).getId());

    factory.createGModel();
    assertNotNull(root);
    verify(modelState).updateRoot(any());
  }

  private static void inject(Object target, String fieldName, Object value) throws Exception {
    Field field = findField(target.getClass(), fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static Field findField(Class<?> type, String fieldName) throws Exception {
    Class<?> current = type;
    while (current != null) {
      try {
        return current.getDeclaredField(fieldName);
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException(fieldName);
  }
}
