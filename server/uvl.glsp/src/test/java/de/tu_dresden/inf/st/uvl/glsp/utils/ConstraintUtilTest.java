/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.AndConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.ImplicationConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.LiteralConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.NotConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.ParenthesisConstraint;
import java.util.Collection;
import org.junit.jupiter.api.Test;

class ConstraintUtilTest {

  @Test
  void excludesConstraintsStayInEdgeClassificationWhenParenthesized() {
    Feature left = new Feature("A");
    Feature right = new Feature("B");

    LiteralConstraint directLeft = new LiteralConstraint(left);
    LiteralConstraint directRight = new LiteralConstraint(right);
    Constraint directExclude = new NotConstraint(new AndConstraint(directLeft, directRight));

    LiteralConstraint wrappedLeft = new LiteralConstraint(left);
    LiteralConstraint wrappedRight = new LiteralConstraint(right);
    Constraint wrappedExclude =
        new NotConstraint(new ParenthesisConstraint(new AndConstraint(wrappedLeft, wrappedRight)));

    FeatureModel featureModel = new FeatureModel();
    featureModel.getOwnConstraints().add(directExclude);
    featureModel.getOwnConstraints().add(wrappedExclude);

    Collection<Constraint> edgeConstraints = FeatureModelUtil.getEdgeConstraints(featureModel);

    assertEquals(2, edgeConstraints.size(), "Both excludes forms should stay edge constraints");
    assertTrue(
        edgeConstraints.contains(directExclude), "Direct excludes should be classified as an edge");
    assertTrue(
        edgeConstraints.contains(wrappedExclude),
        "Parenthesized excludes should be classified as an edge");
    assertTrue(
        FeatureModelUtil.getComplexConstraints(featureModel).isEmpty(),
        "No excludes should be left in the constraint box");
    assertEquals(
        UVLModelTypes.EXCLUDES, ConstraintUtil.convertConstraintTypeToModelType(wrappedExclude));
    assertSame(left, ConstraintUtil.getBiConstraintSource(wrappedExclude).getReference());
    assertSame(right, ConstraintUtil.getBiConstraintTarget(wrappedExclude).getReference());
  }

  @Test
  void requiresConstraintsAndLiteralLookupAreDetectedCorrectly() {
    Feature source = new Feature("Source");
    Feature target = new Feature("Target");
    LiteralConstraint sourceConstraint = new LiteralConstraint(source);
    LiteralConstraint targetConstraint = new LiteralConstraint(target);
    Constraint requires = new ImplicationConstraint(sourceConstraint, targetConstraint);

    FeatureModel featureModel = new FeatureModel();
    featureModel.getLiteralConstraints().add(sourceConstraint);
    featureModel.getLiteralConstraints().add(targetConstraint);
    featureModel.getOwnConstraints().add(requires);

    assertTrue(ConstraintUtil.featureIsInConstraint(source, featureModel));
    assertSame(sourceConstraint, ConstraintUtil.getLiteralConstraint(source, featureModel));
    assertTrue(ConstraintUtil.isRequiresConstraint(requires));
    assertEquals(UVLModelTypes.REQUIRES, ConstraintUtil.convertConstraintTypeToModelType(requires));
    assertSame(sourceConstraint, ConstraintUtil.getBiConstraintSource(requires));
    assertSame(targetConstraint, ConstraintUtil.getBiConstraintTarget(requires));
    assertFalse(ConstraintUtil.isExcludesConstraint(requires));
  }
}
