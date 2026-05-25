/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.gmodel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import java.lang.reflect.Field;
import java.util.Optional;
import org.eclipse.glsp.graph.GNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UVLFeatureFactoryTest {

  private UVLFeatureFactory featureFactory;
  private UVLModelIndex modelIndex;

  @BeforeEach
  void setUp() throws IllegalAccessException {
    featureFactory = new UVLFeatureFactory();
    UVLModelState modelState = mock(UVLModelState.class);
    modelIndex = mock(UVLModelIndex.class);
    UVLAttributeFactory attributeFactory = mock(UVLAttributeFactory.class);

    when(modelState.getIndex()).thenReturn(modelIndex);

    // Inject modelState using reflection since it's @Inject
    try {
      Field modelStateField = findField(featureFactory.getClass());
      modelStateField.setAccessible(true);
      modelStateField.set(featureFactory, modelState);

      // Inject attributeFactory using reflection
      Field attributeField = featureFactory.getClass().getDeclaredField("attributeFactory");
      attributeField.setAccessible(true);
      attributeField.set(featureFactory, attributeFactory);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException("Could not find required field for injection", e);
    }
  }

  private Field findField(Class<?> clazz) throws NoSuchFieldException {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField("modelState");
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException("modelState" + " not found in class hierarchy");
  }

  @Test
  void submodelRootFeatureGetsSubmodelRootCssClass() {
    Feature submodelRootFeature = new Feature("SubmodelRoot");
    submodelRootFeature.setSubmodelRoot(true);

    when(modelIndex.getIdFor(submodelRootFeature)).thenReturn(Optional.of("feature_1"));

    GNode node = featureFactory.create(submodelRootFeature);

    assertTrue(
        hasCssClass(node), "Submodel root features should have the CSS class 'submodel-root'");
    assertEquals(UVLModelTypes.FEATURE, node.getType(), "Node type should be FEATURE");
  }

  @Test
  void normalFeatureDoesNotGetSubmodelRootCssClass() {
    Feature normalFeature = new Feature("NormalFeature");

    when(modelIndex.getIdFor(normalFeature)).thenReturn(Optional.of("feature_2"));

    GNode node = featureFactory.create(normalFeature);

    assertFalse(hasCssClass(node), "Normal features should not have the CSS class 'submodel-root'");
    assertEquals(UVLModelTypes.FEATURE, node.getType(), "Node type should be FEATURE");
  }

  private boolean hasCssClass(GNode node) {
    return node.getCssClasses() != null && node.getCssClasses().contains("submodel-root");
  }
}
