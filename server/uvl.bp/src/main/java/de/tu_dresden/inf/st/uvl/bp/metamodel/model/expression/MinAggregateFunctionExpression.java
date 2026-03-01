package de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression;

import de.tu_dresden.inf.st.uvl.bp.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.GlobalAttribute;
import de.tu_dresden.inf.st.uvl.bp.metamodel.util.Constants;


import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MinAggregateFunctionExpression extends AggregateFunctionExpression {

    /**
     * Evaluates the minimum of the values of the selected features.
     *
     * @param selectedFeatures The set of selected features.
     * @return The minimum of the values of the selected features.
     */
    @Override
    public double evaluate(Set<Feature> selectedFeatures) {
        double min = Double.POSITIVE_INFINITY;
        for (Feature feature : selectedFeatures) {
            Attribute<?> attribute = feature.getAttributes().get(getAttribute().getIdentifier());
            if (attribute != null && attribute.getValue() instanceof Number) {
                min = Math.min(min, ((Number) attribute.getValue()).doubleValue());
            }
        }
        return min == Double.POSITIVE_INFINITY ? 0 : min;
    }

    public MinAggregateFunctionExpression(GlobalAttribute attribute) {
        super(attribute);
    }

    public MinAggregateFunctionExpression(GlobalAttribute attribute, Feature rootFeature) {
        super(attribute, rootFeature);
    }

    @Override
    public String toString(boolean withSubmodels, String currentAlias) {
        return super.toString(withSubmodels, "min", currentAlias);
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
        return new MinAggregateFunctionExpression(attribute, rootFeature);
    }
}
