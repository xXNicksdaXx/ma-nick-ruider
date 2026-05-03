/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.utils;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.AndConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.ExpressionConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.ImplicationConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.LiteralConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.NotConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.expression.Expression;
import de.tu_dresden.inf.st.uvl.metamodel.model.expression.LiteralExpression;
import java.util.List;

public final class ConstraintUtil {

  private ConstraintUtil() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  public static boolean featureIsInConstraint(Feature feature, FeatureModel featureModel) {
    for (LiteralConstraint constraint : featureModel.getLiteralConstraints()) {
      if (constraint.getReference() == feature) {
        return true;
      }
    }
    return false;
  }

  public static LiteralConstraint getLiteralConstraint(Feature feature, FeatureModel featureModel) {
    for (LiteralConstraint constraint : featureModel.getLiteralConstraints()) {
      if (constraint.getReference() == feature) {
        return constraint;
      }
    }
    return null;
  }

  public static boolean featureAttributeIsInConstraint(
      Feature feature, String attribute, FeatureModel featureModel) {
    for (LiteralExpression expression : featureModel.getLiteralExpressions()) {
      if (expression.getContent().getIdentifier().contains(feature.getFeatureName())
          && expression.getContent().getIdentifier().contains(attribute)) {
        return true;
      }
    }
    return false;
  }

  public static void updateFeatureAttributeInConstraints(
      Feature feature,
      String oldAttributeName,
      String newAttributeName,
      FeatureModel featureModel) {
    for (Constraint constraint : featureModel.getOwnConstraints()) {
      if (!(constraint instanceof ExpressionConstraint expressionConstraint)) {
        continue;
      }

      for (Expression expression : expressionConstraint.getExpressionSubParts()) {
        replaceExpression(expression, feature, oldAttributeName, newAttributeName);
      }
    }
  }

  public static void replaceExpression(
      Expression expression, Feature feature, String oldAttributeName, String newAttributeName) {
    for (Expression expressionSubPart : expression.getExpressionSubParts()) {
      if (expressionSubPart instanceof LiteralExpression literalExpression) {
        if (literalExpression.getContent().getIdentifier().contains(feature.getFeatureName())
            && literalExpression.getContent().getIdentifier().contains(oldAttributeName)) {
          // TODO: check new LiteralExpression constructor for correct parameters
          // expression.replaceExpressionSubPart(literalExpression, new LiteralExpression(feature,
          // newAttributeName));
        }
      } else {
        replaceExpression(expressionSubPart, feature, oldAttributeName, newAttributeName);
      }
    }
  }

  public static void removeFeatureAttributeInConstraints(
      Feature feature, String attributeName, FeatureModel featureModel) {
    for (Constraint constraint : featureModel.getOwnConstraints()) {
      if (!(constraint instanceof ExpressionConstraint expressionConstraint)) {
        continue;
      }

      for (Expression expression : expressionConstraint.getExpressionSubParts()) {
        removeExpression(expression, feature, attributeName);
      }
    }
  }

  public static void removeExpression(
      Expression expression, Feature feature, String attributeName) {
    for (Expression expressionSubPart : expression.getExpressionSubParts()) {
      if (expressionSubPart instanceof LiteralExpression literalExpression) {
        if (literalExpression.getContent().getIdentifier().contains(feature.getFeatureName())
            && literalExpression.getContent().getIdentifier().contains(attributeName)) {
          // TODO: check new LiteralExpression constructor for correct parameters
          // expression.replaceExpressionSubPart(literalExpression, new LiteralExpression(feature,
          // ""));
        }
      } else {
        removeExpression(expressionSubPart, feature, attributeName);
      }
    }
  }

  public static List<Constraint> getConstraintsForFeature(
      Feature feature, FeatureModel featureModel) {
    return featureModel.getConstraints().stream()
        .filter(
            constraint ->
                constraint
                    .getConstraintSubParts()
                    .contains(getLiteralConstraint(feature, featureModel)))
        .toList();
  }

  public static String convertConstraintTypeToModelType(Constraint constraint) {
    if (isRequiresConstraint(constraint)) {
      return UVLModelTypes.REQUIRES;
    }
    if (isExcludesConstraint(constraint)) {
      return UVLModelTypes.EXCLUDES;
    }
    return "";
  }

  public static boolean isRequiresConstraint(Constraint constraint) {
    if (!(constraint instanceof ImplicationConstraint)) {
      return false;
    }
    long literalCount =
        constraint.getConstraintSubParts().stream()
            .filter(subConstraint -> subConstraint instanceof LiteralConstraint)
            .count();
    return literalCount == 2;
  }

  public static boolean isExcludesConstraint(Constraint constraint) {
    if (!(constraint instanceof NotConstraint notConstraint)) {
      return false;
    }

    if (!(notConstraint.getContent() instanceof AndConstraint andConstraint)) {
      return false;
    }

    return andConstraint.getChildren().size() == 2
        && andConstraint.getChildren().stream().allMatch(LiteralConstraint.class::isInstance);
  }

  public static LiteralConstraint getBiConstraintSource(Constraint constraint) {
    return getBiConstraintLiteral(constraint, true);
  }

  public static LiteralConstraint getBiConstraintTarget(Constraint constraint) {
    return getBiConstraintLiteral(constraint, false);
  }

  private static LiteralConstraint getBiConstraintLiteral(Constraint constraint, boolean source) {
    if (isRequiresConstraint(constraint)) {
      ImplicationConstraint implicationConstraint = (ImplicationConstraint) constraint;
      Constraint subConstraint =
          source
              ? implicationConstraint.getConstraintSubParts().getFirst()
              : implicationConstraint.getConstraintSubParts().getLast();
      if (subConstraint instanceof LiteralConstraint literalConstraint) {
        return literalConstraint;
      }
      throw new IllegalStateException(
          "Unsupported bi-constraint literal type: " + subConstraint.getClass().getName());
    }

    if (isExcludesConstraint(constraint)) {
      NotConstraint notConstraint = (NotConstraint) constraint;
      AndConstraint andConstraint = (AndConstraint) notConstraint.getContent();
      Constraint subConstraint =
          source ? andConstraint.getChildren().getFirst() : andConstraint.getChildren().getLast();
      if (subConstraint instanceof LiteralConstraint literalConstraint) {
        return literalConstraint;
      }
      throw new IllegalStateException(
          "Unsupported excludes literal type: " + subConstraint.getClass().getName());
    }

    throw new IllegalStateException(
        "Unsupported bi-constraint type: " + constraint.getClass().getName());
  }
}
