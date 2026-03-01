package de.tu_dresden.inf.st.uvl.bp.metamodel.conversion;

import de.tu_dresden.inf.st.uvl.bp.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.LanguageLevel;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.constraint.ExpressionConstraint;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression.Expression;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression.LengthAggregateFunctionExpression;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DropStringConstraints implements IConversionStrategy {
    @Override
    public Set<LanguageLevel> getLevelsToBeRemoved() {
        return new HashSet<>(Collections.singletonList(LanguageLevel.STRING_CONSTRAINTS));
    }

    @Override
    public Set<LanguageLevel> getTargetLevelsOfConversion() {
        return new HashSet<>();
    }

    @Override
    public void convertFeatureModel(final FeatureModel rootFeatureModel, final FeatureModel featureModel) {
        featureModel.getOwnConstraints().removeIf(this::constraintContainsStringAggregateFunction);
    }

    private boolean constraintContainsStringAggregateFunction(final Constraint constraint) {
        if (constraint instanceof ExpressionConstraint) {
            for (final Expression subExpression : ((ExpressionConstraint) constraint).getExpressionSubParts()) {
                if (this.expressionContainsStringAggregateFunction(subExpression)) {
                    return true;
                }
            }
        } else {
            for (final Constraint subConstraint : constraint.getConstraintSubParts()) {
                if (this.constraintContainsStringAggregateFunction(subConstraint)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean expressionContainsStringAggregateFunction(final Expression expression) {
        if (expression instanceof LengthAggregateFunctionExpression) {
            return true;
        } else {
            for (final Expression subExpression : expression.getExpressionSubParts()) {
                if (this.expressionContainsStringAggregateFunction(subExpression)) {
                    return true;
                }
            }
        }
        return false;
    }
}
