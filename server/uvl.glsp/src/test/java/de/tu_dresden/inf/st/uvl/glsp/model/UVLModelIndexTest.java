/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group.GroupType;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.ImplicationConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.LiteralConstraint;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.GCompartmentBuilder;
import org.eclipse.glsp.graph.builder.impl.GEdgeBuilder;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.junit.jupiter.api.Test;

class UVLModelIndexTest {

  @Test
  void givenMatchingGModelElements_whenIndexFeatureModel_thenResolvesUvLObjectsByIds()
      throws Exception {
    // Arrange
    Feature root = new Feature("Root");
    Feature child = new Feature("Child");
    Group group = new Group(GroupType.MANDATORY);
    group.setParentFeature(root);
    group.getFeatures().add(child);
    root.getChildren().add(group);

    FeatureModel featureModel = new FeatureModel();
    featureModel.setRootFeature(root);
    featureModel.getFeatureMap().put(root.getFeatureName(), root);
    featureModel.getFeatureMap().put(child.getFeatureName(), child);

    LiteralConstraint left = new LiteralConstraint(root);
    LiteralConstraint right = new LiteralConstraint(child);
    ImplicationConstraint requires = new ImplicationConstraint(left, right);
    featureModel.getOwnConstraints().add(requires);

    GNode rootNode = buildFeatureNode("root-id", "Root");
    GNode childNode = buildFeatureNode("child-id", "Child");
    GEdge groupEdge =
        new GEdgeBuilder(UVLModelTypes.MANDATORY)
            .id("group-id_child-id")
            .sourceId("root-id")
            .targetId("child-id")
            .build();
    GEdge constraintEdge =
        new GEdgeBuilder(UVLModelTypes.REQUIRES)
            .id("constraint-edge")
            .sourceId("root-id")
            .targetId("child-id")
            .build();

    GGraph graph = new GGraphBuilder(DefaultTypes.GRAPH).id("root-graph").build();
    ExposedUVLModelIndex index = new ExposedUVLModelIndex(graph);
    Map<String, GModelElement> idToElement = new HashMap<>();
    idToElement.put(rootNode.getId(), rootNode);
    idToElement.put(childNode.getId(), childNode);
    idToElement.put(groupEdge.getId(), groupEdge);
    idToElement.put(constraintEdge.getId(), constraintEdge);
    injectIdToElement(index, idToElement);

    // Act
    index.exposeIndexFeatureModel(featureModel);

    // Assert
    assertSame(root, index.getUVLObject("root-id").orElseThrow());
    assertSame(child, index.getUVLObject("child-id").orElseThrow());
    assertSame(group, index.getUVLObject("group-id").orElseThrow());
    assertSame(requires, index.getUVLObject("constraint-edge").orElseThrow());
    assertSame(constraintEdge, index.getGModelElement(requires).orElseThrow());
    assertEquals("constraint-edge", index.getIdFor(requires).orElseThrow());
  }

  private static GNode buildFeatureNode(String id, String featureName) {
    GLabel label =
        new GLabelBuilder(UVLModelTypes.FEATURE_NAME).id(id + "_label").text(featureName).build();
    return new GNodeBuilder(UVLModelTypes.FEATURE)
        .id(id)
        .add(new GCompartmentBuilder(DefaultTypes.COMPARTMENT).id(id + "_comp").add(label).build())
        .build();
  }

  private static void injectIdToElement(UVLModelIndex index, Map<String, GModelElement> map)
      throws Exception {
    Field field = findField(index.getClass(), "idToElement");
    field.setAccessible(true);
    field.set(index, map);
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

  private static final class ExposedUVLModelIndex extends UVLModelIndex {
    ExposedUVLModelIndex(EObject target) {
      super(target);
    }

    void exposeIndexFeatureModel(FeatureModel featureModel) {
      indexFeatureModel(featureModel);
    }
  }
}
