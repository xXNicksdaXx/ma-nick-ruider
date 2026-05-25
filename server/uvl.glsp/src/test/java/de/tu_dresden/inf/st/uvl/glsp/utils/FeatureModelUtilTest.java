/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.tu_dresden.inf.st.uvl.metamodel.model.Cardinality;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group.GroupType;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.AndConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.ImplicationConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.LiteralConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.NotConstraint;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

class FeatureModelUtilTest {

  @Test
  void getAllFeatureEdgesIncludesParentAndChildGroupEdges() {
    Feature root = new Feature("Root");
    Feature child = new Feature("Child");
    Group rootGroup = new Group(GroupType.MANDATORY);
    rootGroup.setParentFeature(root);
    root.getChildren().add(rootGroup);
    rootGroup.getFeatures().add(child);

    assertEquals(List.of("Root_mandatory_Child"), FeatureModelUtil.getAllFeatureEdges(child));
    assertEquals(List.of("Root_mandatory_Child"), FeatureModelUtil.getAllFeatureEdges(root));
  }

  @Test
  void getAllGroupsSkipsGroupsInsideSubmodelRootSubtrees() {
    FeatureModel featureModel = new FeatureModel();

    Feature root = new Feature("Root");
    Feature normalChild = new Feature("NormalChild");
    Feature normalGrandChild = new Feature("NormalGrandChild");
    Feature submodelRoot = new Feature("SubmodelRoot");
    submodelRoot.setSubmodelRoot(true);
    Feature hiddenChild = new Feature("HiddenChild");

    Group rootGroup = new Group(GroupType.MANDATORY);
    Group normalChildGroup = new Group(GroupType.MANDATORY);
    Group submodelGroup = new Group(GroupType.OPTIONAL);

    featureModel.setRootFeature(root);
    featureModel.getFeatureMap().put(root.getFeatureName(), root);
    featureModel.getFeatureMap().put(normalChild.getFeatureName(), normalChild);
    featureModel.getFeatureMap().put(normalGrandChild.getFeatureName(), normalGrandChild);
    featureModel.getFeatureMap().put(submodelRoot.getFeatureName(), submodelRoot);
    featureModel.getFeatureMap().put(hiddenChild.getFeatureName(), hiddenChild);

    root.getChildren().add(rootGroup);
    rootGroup.getFeatures().add(normalChild);
    rootGroup.getFeatures().add(submodelRoot);

    normalChild.getChildren().add(normalChildGroup);
    normalChildGroup.getFeatures().add(normalGrandChild);

    submodelRoot.getChildren().add(submodelGroup);
    submodelGroup.getFeatures().add(hiddenChild);

    Collection<Feature> visibleFeatures = FeatureModelUtil.getVisibleFeatures(featureModel);
    Collection<Group> groups = FeatureModelUtil.getAllGroups(featureModel);

    assertEquals(4, visibleFeatures.size(), "Only visible features should be returned");
    assertTrue(visibleFeatures.contains(root), "The root feature should be visible");
    assertTrue(
        visibleFeatures.contains(normalChild), "Normal visible descendants should be returned");
    assertTrue(
        visibleFeatures.contains(normalGrandChild), "Deeper normal descendants should be returned");
    assertTrue(
        visibleFeatures.contains(submodelRoot), "The submodel root itself should remain visible");
    assertFalse(visibleFeatures.contains(hiddenChild), "Submodel descendants must be hidden");

    assertEquals(2, groups.size(), "Only visible groups should be returned");
    assertTrue(groups.contains(rootGroup), "The visible root group should be returned");
    assertTrue(
        groups.contains(normalChildGroup), "Groups below normal features should be returned");
    assertFalse(
        groups.contains(submodelGroup), "Groups inside submodel-root subtrees must be skipped");
  }

  @Test
  void edgeAndComplexConstraintClassificationFollowsConstraintKinds() {
    Feature left = new Feature("Left");
    Feature right = new Feature("Right");
    LiteralConstraint leftLiteral = new LiteralConstraint(left);
    LiteralConstraint rightLiteral = new LiteralConstraint(right);
    Constraint requires = new ImplicationConstraint(leftLiteral, rightLiteral);
    Constraint excludes = new NotConstraint(new AndConstraint(leftLiteral, rightLiteral));
    Constraint complex = new LiteralConstraint(left);

    FeatureModel featureModel = new FeatureModel();
    featureModel.getOwnConstraints().add(requires);
    featureModel.getOwnConstraints().add(excludes);
    featureModel.getOwnConstraints().add(complex);

    Collection<Constraint> edgeConstraints = FeatureModelUtil.getEdgeConstraints(featureModel);
    Collection<Constraint> complexConstraints =
        FeatureModelUtil.getComplexConstraints(featureModel);

    assertEquals(2, edgeConstraints.size());
    assertTrue(edgeConstraints.contains(requires));
    assertTrue(edgeConstraints.contains(excludes));
    assertEquals(List.of(complex), complexConstraints);
  }

  @Test
  void cardinalityHelpersSupportNullAndUnboundedRanges() {
    assertNull(FeatureModelUtil.createCardinality(null));
    assertNull(FeatureModelUtil.createCardinality(""));

    Cardinality cardinality = FeatureModelUtil.createCardinality("1..*");
    assertEquals(1, cardinality.lower);
    assertEquals(Integer.MAX_VALUE, cardinality.upper);
    assertEquals("1..*", FeatureModelUtil.getCardinalityText(cardinality));
    assertEquals("", FeatureModelUtil.getCardinalityText(null));
  }

  @Test
  void includesFeatureCardinalityDetectsVisibleFeaturesOnly() {
    FeatureModel featureModel = new FeatureModel();
    Feature root = new Feature("Root");
    root.setCardinality(new Cardinality(1));
    featureModel.setRootFeature(root);
    featureModel.getFeatureMap().put(root.getFeatureName(), root);

    assertTrue(FeatureModelUtil.includesFeatureCardinality(featureModel));

    root.setCardinality(null);
    assertFalse(FeatureModelUtil.includesFeatureCardinality(featureModel));
  }
}
