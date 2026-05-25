/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.LiteralConstraint;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.junit.jupiter.api.Test;

class UVLApplyLabelEditOperationHandlerTest {

  @Test
  void givenDuplicateAttributeName_whenUpdateAttributeName_thenThrows() {
    // Arrange
    UVLApplyLabelEditOperationHandler handler = new UVLApplyLabelEditOperationHandler();
    Feature feature = new Feature("Feature");
    feature.getAttributes().put("old", new Attribute<>("old", 1, feature));
    feature.getAttributes().put("existing", new Attribute<>("existing", 2, feature));
    GLabel label =
        new GLabelBuilder(UVLModelTypes.ATTRIBUTE_NAME)
            .id("feature-id_attribute[old]_name")
            .text("old")
            .build();

    // Act + Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> handler.updateAttributeName(label, feature, "existing"));
  }

  @Test
  void givenAttributeValueEdit_whenUpdateAttributeValue_thenCastsAndUpdatesLabel() {
    // Arrange
    UVLApplyLabelEditOperationHandler handler = new UVLApplyLabelEditOperationHandler();
    Feature feature = new Feature("Feature");
    feature.getAttributes().put("flag", new Attribute<>("flag", "false", feature));
    GLabel label =
        new GLabelBuilder(UVLModelTypes.ATTRIBUTE_VALUE)
            .id("feature-id_attribute[flag]_value")
            .text("false")
            .build();

    // Act
    handler.updateAttributeValue(label, feature, "true");

    // Assert
    assertEquals("true", label.getText());
    assertEquals(Boolean.TRUE, feature.getAttributes().get("flag").getValue());
  }

  @Test
  void givenConstraintEdit_whenUpdateConstraint_thenReplacesConstraintAndUpdatesLabel() {
    // Arrange
    Feature root = new Feature("Root");
    FeatureModel featureModel = new FeatureModel();
    featureModel.setRootFeature(root);
    Constraint existing = new LiteralConstraint(root);
    featureModel.getOwnConstraints().add(existing);

    UVLModelState modelState = mock(UVLModelState.class);
    when(modelState.getFeatureModel()).thenReturn(featureModel);

    Constraint replacement = new LiteralConstraint(new Feature("Other"));
    TestableApplyLabelEditHandler handler = new TestableApplyLabelEditHandler(replacement);
    handler.modelState = modelState;

    GLabel label =
        new GLabelBuilder(UVLModelTypes.CONSTRAINT_TEXT)
            .id("constraint-text")
            .text(existing.toString())
            .build();

    // Act
    handler.updateConstraint(label, existing, "Other");

    // Assert
    assertSame(replacement, featureModel.getOwnConstraints().getFirst());
    assertEquals(replacement.toString(), label.getText());
  }

  @Test
  void givenInvalidCardinality_whenUpdateFeatureCardinality_thenThrows() {
    // Arrange
    UVLApplyLabelEditOperationHandler handler = new UVLApplyLabelEditOperationHandler();
    Feature feature = new Feature("Feature");
    GLabel label = new GLabelBuilder(UVLModelTypes.CARDINALITY_LABEL).id("id").text("0..1").build();

    // Act + Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> handler.updateFeatureCardinality(label, feature, "invalid"));
  }

  private static final class TestableApplyLabelEditHandler
      extends UVLApplyLabelEditOperationHandler {
    private final Constraint parsedConstraint;

    private TestableApplyLabelEditHandler(Constraint parsedConstraint) {
      this.parsedConstraint = parsedConstraint;
    }

    @Override
    protected Constraint parseConstraint(String newConstraint) {
      return parsedConstraint;
    }
  }
}
