/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.actions.UVLComputedBoundsActionHandler;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group.GroupType;
import de.tu_dresden.inf.st.uvl.metamodel.model.LanguageLevel;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.ImplicationConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.LiteralConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.NotConstraint;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.GEdgeBuilder;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.eclipse.glsp.server.operations.CreateEdgeOperation;
import org.eclipse.glsp.server.operations.CreateNodeOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UVLHandlerUtilitiesTest {

  private UVLModelState modelState;
  private UVLModelIndex modelIndex;

  @BeforeEach
  void setUp() {
    modelState = mock(UVLModelState.class);
    modelIndex = mock(UVLModelIndex.class);
    when(modelState.getIndex()).thenReturn(modelIndex);
  }

  @Test
  void createAttributeHandlerResolvesNestedAttributeTargets() throws Exception {
    UVLCreateAttributeOperationHandler handler = new UVLCreateAttributeOperationHandler();
    injectModelState(handler, modelState);

    Feature feature = new Feature("Feature");
    Map<String, Attribute<?>> nested = new LinkedHashMap<>();
    nested.put("inner", new Attribute<>("inner", 7, feature));
    feature.getAttributes().put("settings", new Attribute<>("settings", nested, feature));

    Map<String, Attribute<?>> resolved =
        handler.resolveTargetAttributes(feature, "feature-id_attribute[settings]");
    assertSame(nested, resolved);
    assertEquals(
        "Attribute2",
        handler.generateAttributeName(
            Map.of("Attribute1", new Attribute<>("Attribute1", true, feature))));

    handler.createAttribute(feature, "feature-id");
    assertTrue(feature.getAttributes().containsKey("Attribute2"));
  }

  @Test
  void featureCreationHandlerCreatesNestedFeaturesAndDetectsMissingRoot() throws Exception {
    UVLCreateFeatureOperationHandler handler = new UVLCreateFeatureOperationHandler();
    injectModelState(handler, modelState);

    FeatureModel featureModel = new FeatureModel();
    when(modelState.getFeatureModel()).thenReturn(featureModel);
    assertTrue(handler.isMissingRootFeature());

    Feature parent = new Feature("Parent");
    Group group = new Group(GroupType.MANDATORY);
    group.setParentFeature(parent);
    parent.getChildren().add(group);

    handler.createFeature(parent);
    assertEquals(1, group.getFeatures().size());
    assertEquals("Feature1", group.getFeatures().getFirst().getFeatureName());

    Feature root = new Feature("Root");
    featureModel.setRootFeature(root);
    featureModel.getFeatureMap().put(root.getFeatureName(), root);
    featureModel.getFeatureMap().put("Feature1", group.getFeatures().getFirst());
    assertFalse(handler.isMissingRootFeature());
  }

  @Test
  void featureCardinalityHandlerSetsDefaultCardinalityAndLanguageLevel() throws Exception {
    UVLCreateFeatureCardinalityOperationHandler handler =
        new UVLCreateFeatureCardinalityOperationHandler();
    injectModelState(handler, modelState);

    FeatureModel featureModel = new FeatureModel();
    when(modelState.getFeatureModel()).thenReturn(featureModel);
    Feature feature = new Feature("Feature");

    handler.createFeatureCardinality(feature);

    assertNotNull(feature.getCardinality());
    assertTrue(featureModel.getUsedLanguageLevels().contains(LanguageLevel.FEATURE_CARDINALITY));
  }

  @Test
  void relationEdgeHandlerCreatesAndRemovesGroups() throws Exception {
    UVLCreateRelationEdgeOperationHandler handler = new UVLCreateRelationEdgeOperationHandler();
    injectModelState(handler, modelState);

    Feature source = new Feature("Source");
    Feature target = new Feature("Target");
    when(modelIndex.getUVLObject("source-id", Feature.class)).thenReturn(Optional.of(source));
    when(modelIndex.getUVLObject("target-id", Feature.class)).thenReturn(Optional.of(target));

    Group group = new Group(GroupType.OPTIONAL);
    group.setParentFeature(source);
    group.getFeatures().add(target);
    source.getChildren().add(group);

    GEdge edge =
        new GEdgeBuilder(UVLModelTypes.OPTIONAL)
            .id("edge-id")
            .sourceId("source-id")
            .targetId("target-id")
            .build();
    GGraph root = new GGraphBuilder(DefaultTypes.GRAPH).id("root").build();
    root.getChildren().add(edge);
    when(modelState.getRoot()).thenReturn(root);

    handler.createNewGroup(source, target, GroupType.MANDATORY);
    assertTrue(handler.findExistingGroup(source, GroupType.MANDATORY).isPresent());

    handler.removeExistingEdge(source, target, edge);
    assertFalse(root.getChildren().contains(edge));
    assertTrue(group.getFeatures().isEmpty());
  }

  @Test
  void biConstraintEdgeHandlerCreatesBinaryConstraints() throws Exception {
    UVLCreateBiConstraintEdgeOperationHandler handler =
        new UVLCreateBiConstraintEdgeOperationHandler();
    injectModelState(handler, modelState);

    Feature source = new Feature("Source");
    Feature target = new Feature("Target");
    when(modelIndex.getUVLObject("source-id", Feature.class)).thenReturn(Optional.of(source));
    when(modelIndex.getUVLObject("target-id", Feature.class)).thenReturn(Optional.of(target));

    CreateEdgeOperation requiresOperation = mock(CreateEdgeOperation.class);
    when(requiresOperation.getElementTypeId()).thenReturn(UVLModelTypes.REQUIRES);
    when(requiresOperation.getSourceElementId()).thenReturn("source-id");
    when(requiresOperation.getTargetElementId()).thenReturn("target-id");

    LiteralConstraint sourceConstraint = handler.getSourceConstraint(requiresOperation);
    LiteralConstraint targetConstraint = handler.getTargetConstraint(requiresOperation);
    assertSame(source, sourceConstraint.getReference());
    assertSame(target, targetConstraint.getReference());

    FeatureModel featureModel = new FeatureModel();
    featureModel.getFeatureMap().put(source.getFeatureName(), source);
    featureModel.getFeatureMap().put(target.getFeatureName(), target);
    when(modelState.getFeatureModel()).thenReturn(featureModel);

    handler.executeCreation(requiresOperation);
    assertEquals(2, featureModel.getLiteralConstraints().size());
    assertInstanceOf(ImplicationConstraint.class, featureModel.getOwnConstraints().getFirst());

    CreateEdgeOperation excludesOperation = mock(CreateEdgeOperation.class);
    when(excludesOperation.getElementTypeId()).thenReturn(UVLModelTypes.EXCLUDES);
    when(excludesOperation.getSourceElementId()).thenReturn("source-id");
    when(excludesOperation.getTargetElementId()).thenReturn("target-id");
    handler.executeCreation(excludesOperation);
    assertInstanceOf(NotConstraint.class, featureModel.getOwnConstraints().getLast());
  }

  @Test
  void complexConstraintHandlerAddsDefaultConstraintOnlyOnce() throws Exception {
    UVLCreateComplexConstraintOperationHandler handler =
        new UVLCreateComplexConstraintOperationHandler();
    injectModelState(handler, modelState);

    Feature root = new Feature("Root");
    FeatureModel featureModel = new FeatureModel();
    featureModel.setRootFeature(root);
    when(modelState.getFeatureModel()).thenReturn(featureModel);

    CreateNodeOperation operation = mock(CreateNodeOperation.class);
    when(operation.getContainerId()).thenReturn("constraint_box");

    handler.executeCreation(operation);
    handler.executeCreation(operation);

    assertEquals(1, featureModel.getOwnConstraints().size());
    assertInstanceOf(LiteralConstraint.class, featureModel.getOwnConstraints().getFirst());
  }

  @Test
  void deleteOperationHandlerRemovesObjectsAndAttributeElements() throws Exception {
    UVLDeleteOperationHandler handler = new UVLDeleteOperationHandler();
    injectModelState(handler, modelState);

    FeatureModel featureModel = new FeatureModel();
    Feature parent = new Feature("Parent");
    Feature child = new Feature("Child");
    Group group = new Group(GroupType.MANDATORY);
    group.setParentFeature(parent);
    group.getFeatures().add(child);
    parent.getChildren().add(group);
    featureModel.setRootFeature(parent);
    featureModel.getFeatureMap().put(parent.getFeatureName(), parent);
    featureModel.getFeatureMap().put(child.getFeatureName(), child);
    when(modelState.getFeatureModel()).thenReturn(featureModel);

    Constraint constraint = new LiteralConstraint(parent);
    featureModel.getOwnConstraints().add(constraint);

    handler.deleteUvlObject(child);
    assertFalse(featureModel.getFeatureMap().containsKey(child.getFeatureName()));
    assertTrue(parent.getChildren().isEmpty());

    handler.deleteUvlObject(constraint);
    assertTrue(featureModel.getOwnConstraints().isEmpty());
    assertTrue(handler.isDeletableAttributeElement(UVLModelTypes.ATTRIBUTE));
  }

  @Test
  void applyLabelEditHandlerReplacesMapEntriesAndUpdatesCardinality() throws Exception {
    UVLApplyLabelEditOperationHandler handler = new UVLApplyLabelEditOperationHandler();
    injectModelState(handler, modelState);

    FeatureModel featureModel = new FeatureModel();
    Feature feature = new Feature("Feature");
    featureModel.setRootFeature(feature);
    when(modelState.getFeatureModel()).thenReturn(featureModel);

    Map<String, Attribute<?>> attributes = new LinkedHashMap<>();
    Attribute<Integer> oldAttribute = new Attribute<>("old", 1, feature);
    attributes.put("old", oldAttribute);
    handler.replaceMapEntry(attributes, "old", "new", new Attribute<>("new", 2, feature));
    assertTrue(attributes.containsKey("new"));
    assertFalse(attributes.containsKey("old"));

    GLabel label = new GLabelBuilder(UVLModelTypes.CARDINALITY_LABEL).id("id").text("0..1").build();
    handler.updateFeatureCardinality(label, feature, "1..*");
    assertEquals("1..*", label.getText());
    assertEquals("1..*", FeatureModelUtil.getCardinalityText(feature.getCardinality()));

    featureModel.getUsedLanguageLevels().add(LanguageLevel.FEATURE_CARDINALITY);
    handler.updateFeatureCardinality(label, feature, "");
    assertNull(feature.getCardinality());
    assertFalse(featureModel.getUsedLanguageLevels().contains(LanguageLevel.FEATURE_CARDINALITY));

    Group group = new Group(GroupType.OPTIONAL);
    GLabel groupLabel =
        new GLabelBuilder(UVLModelTypes.CARDINALITY_LABEL).id("id2").text("0..1").build();
    handler.updateGroupCardinality(groupLabel, group, "0..*");
    assertEquals("0..*", groupLabel.getText());
    assertEquals("0..*", FeatureModelUtil.getCardinalityText(group.getCardinality()));
    assertThrows(
        IllegalArgumentException.class,
        () -> handler.updateGroupCardinality(groupLabel, group, "bad"));
  }

  @Test
  void computedBoundsHandlerRequestsRelayoutWhenTooManyNodesStartAtOrigin() throws Exception {
    ExposedComputedBoundsActionHandler handler = new ExposedComputedBoundsActionHandler();
    injectModelState(handler, modelState);

    GGraph root = new GGraphBuilder(DefaultTypes.GRAPH).id("root").build();
    List<GNode> nodes = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      GNode node =
          new GNodeBuilder(UVLModelTypes.FEATURE)
              .id("node-" + i)
              .position(0, 0)
              .size(10, 10)
              .build();
      root.getChildren().add(node);
      nodes.add(node);
    }
    when(modelIndex.getStream(root)).thenReturn(nodes.stream().map(node -> node));

    assertTrue(handler.exposesRequiresRelayout(root));
  }

  private static void injectModelState(Object target, Object value) throws Exception {
    Field field = findField(target.getClass());
    field.setAccessible(true);
    field.set(target, value);
  }

  private static Field findField(Class<?> type) throws Exception {
    Class<?> current = type;
    while (current != null) {
      try {
        return current.getDeclaredField("modelState");
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException("modelState");
  }

  private static final class ExposedComputedBoundsActionHandler
      extends UVLComputedBoundsActionHandler {
    boolean exposesRequiresRelayout(GGraph root) {
      return requiresRelayout(root);
    }
  }
}
