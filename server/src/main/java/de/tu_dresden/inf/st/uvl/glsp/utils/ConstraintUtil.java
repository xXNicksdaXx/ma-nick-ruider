/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.utils;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.constraint.*;
import de.vill.model.expression.Expression;
import de.vill.model.expression.LiteralExpression;

import java.util.List;

public class ConstraintUtil {

    public static boolean featureIsInConstraint(Feature feature, FeatureModel featureModel) {
        for (LiteralConstraint constraint : featureModel.getLiteralConstraints()) {
            if (constraint.getFeature() == feature) {
                return true;
            }
        }
        return false;
    }

    public static LiteralConstraint getLiteralConstraint(Feature feature, FeatureModel featureModel) {
        for (LiteralConstraint constraint : featureModel.getLiteralConstraints()) {
            if (constraint.getFeature() == feature) {
                return constraint;
            }
        }
        return null;
    }

    public static boolean featureAttributeIsInConstraint(Feature feature, String attribute, FeatureModel featureModel) {
        for (LiteralExpression expression : featureModel.getLiteralExpressions()) {
            if (expression.getFeatureName().equals(feature.getFeatureName()) && expression.getAttributeName().equals(attribute)) {
                return true;
            }
        }
        return false;
    }

    public static void updateFeatureAttributeInConstraints(Feature feature, String oldAttributeName, String newAttributeName, FeatureModel featureModel) {
        for (Constraint constraint : featureModel.getOwnConstraints()) {
            if (!(constraint instanceof ExpressionConstraint expressionConstraint)) {
                continue;
            }

            for (Expression expression : expressionConstraint.getExpressionSubParts()) {
                replaceExpression(expression, feature, oldAttributeName, newAttributeName);
            }
        }
    }

    public static void replaceExpression(Expression expression, Feature feature, String oldAttributeName, String newAttributeName) {
        for (Expression expressionSubPart : expression.getExpressionSubParts()) {
            if (expressionSubPart instanceof LiteralExpression literalExpression) {
                if (literalExpression.getFeatureName().equals(feature.getFeatureName()) && literalExpression.getAttributeName().equals(oldAttributeName)) {
                    expression.replaceExpressionSubPart(literalExpression, new LiteralExpression(feature, newAttributeName));
                }
            } else {
                replaceExpression(expressionSubPart, feature, oldAttributeName, newAttributeName);
            }
        }
    }

    public static List<Constraint> getConstraintsForFeature(Feature feature, FeatureModel featureModel) {
        return featureModel.getConstraints().stream()
                .filter(constraint -> constraint.getConstraintSubParts().contains(getLiteralConstraint(feature, featureModel)))
                .toList();
    }

    public static String convertConstraintTypeToModelType(Constraint constraint) {
        return switch (constraint) {
            case ImplicationConstraint ignored -> UVLModelTypes.IMPLICATION;
            case EquivalenceConstraint ignored -> UVLModelTypes.EQUIVALENCE;
            default -> "";
        };
    }
}
