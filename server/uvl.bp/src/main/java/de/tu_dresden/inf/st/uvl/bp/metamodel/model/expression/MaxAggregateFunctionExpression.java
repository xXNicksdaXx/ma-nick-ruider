package de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression;

import de.tu_dresden.inf.st.uvl.bp.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.GlobalAttribute;
import de.tu_dresden.inf.st.uvl.bp.metamodel.util.Constants;


import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MaxAggregateFunctionExpression extends AggregateFunctionExpression {
    /**
     * Evaluates the maximum of the values of the selected features.
     *
     * @param selectedFeatures The set of selected features.
     * @return The maximum of the values of the selected features.
     */
    @Override
    public double evaluate(Set<Feature> selectedFeatures) {
        double max = Double.NEGATIVE_INFINITY;
        for (Feature feature : selectedFeatures) {
            Attribute<?> attribute = feature.getAttributes().get(getAttribute().getIdentifier());
            if (attribute != null && attribute.getValue() instanceof Number) {
                max = Math.max(max, ((Number) attribute.getValue()).doubleValue());
            }
        }
        return max == Double.NEGATIVE_INFINITY ? 0 : max;
    }
    
    public MaxAggregateFunctionExpression(GlobalAttribute attribute) {
        super(attribute);
    }

    public MaxAggregateFunctionExpression(GlobalAttribute attribute, Feature rootFeature) {
        super(attribute, rootFeature);
    }

    @Override
    public String toString(boolean withSubmodels, String currentAlias) {
        return super.toString(withSubmodels, "max", currentAlias);
    }

    @Override
    public String getReturnType() {
        return Constants.NUMBER;
    }

    @Override
    public List<Expression> getExpressionSubParts() {
        return Arrays.asList();
    }

    @Override
    public Expression clone() {
        return new MaxAggregateFunctionExpression(attribute, rootFeature);
    }
}
