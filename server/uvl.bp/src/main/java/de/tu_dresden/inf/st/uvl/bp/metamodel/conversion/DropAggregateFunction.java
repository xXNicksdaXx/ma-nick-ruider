package de.tu_dresden.inf.st.uvl.bp.metamodel.conversion;

import de.tu_dresden.inf.st.uvl.bp.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.LanguageLevel;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.constraint.ExpressionConstraint;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression.AvgAggregateFunctionExpression;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression.Expression;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression.SumAggregateFunctionExpression;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DropAggregateFunction implements IConversionStrategy {
    @Override
    public Set<LanguageLevel> getLevelsToBeRemoved() {
        return new HashSet<>(Arrays.asList(LanguageLevel.AGGREGATE_FUNCTION));
    }

    @Override
    public Set<LanguageLevel> getTargetLevelsOfConversion() {
        return new HashSet<>();
    }

    @Override
    public void convertFeatureModel(final FeatureModel rootFeatureModel, final FeatureModel featureModel) {
        featureModel.getOwnConstraints().removeIf(this::constraintContainsAggregateFunction);
    }

    private boolean constraintContainsAggregateFunction(final Constraint constraint) {
        if (constraint instanceof ExpressionConstraint) {
            for (final Expression subExpression : ((ExpressionConstraint) constraint).getExpressionSubParts()) {
                if (expressionContainsAggregateFunction(subExpression)) {
                    return true;
                }
            }
        } else {
            for (final Constraint subConstraint : constraint.getConstraintSubParts()) {
                if (constraintContainsAggregateFunction(subConstraint)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean expressionContainsAggregateFunction(final Expression expression) {
        if (expression instanceof AvgAggregateFunctionExpression || expression instanceof SumAggregateFunctionExpression) {
            return true;
        } else {
            for (final Expression subExpression : expression.getExpressionSubParts()) {
                if (expressionContainsAggregateFunction(subExpression)) {
                    return true;
                }
            }
        }
        return false;
    }
}
