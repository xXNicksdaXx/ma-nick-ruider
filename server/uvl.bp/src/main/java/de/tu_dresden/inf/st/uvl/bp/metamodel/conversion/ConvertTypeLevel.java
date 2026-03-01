package de.tu_dresden.inf.st.uvl.bp.metamodel.conversion;

import de.tu_dresden.inf.st.uvl.bp.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.Group;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.LanguageLevel;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.constraint.ExpressionConstraint;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression.Expression;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression.LiteralExpression;
import de.tu_dresden.inf.st.uvl.bp.metamodel.util.Constants;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ConvertTypeLevel implements IConversionStrategy {
    private FeatureModel rootFeatureModel;

    @Override
    public Set<LanguageLevel> getLevelsToBeRemoved() {
        return new HashSet<>(Collections.singletonList(LanguageLevel.TYPE_LEVEL));
    }

    @Override
    public Set<LanguageLevel> getTargetLevelsOfConversion() {
        return new HashSet<>(Collections.singletonList(LanguageLevel.ARITHMETIC_LEVEL));
    }

    @Override
    public void convertFeatureModel(final FeatureModel rootFeatureModel, final FeatureModel featureModel) {
        this.rootFeatureModel = rootFeatureModel;
        this.rootFeatureModel.getOwnConstraints().forEach(this::convertFeaturesInExpressionConstraint);
        this.traverseFeatures(featureModel.getRootFeature());
    }

    private void convertFeaturesInExpressionConstraint(final Constraint constraint) {
        if (constraint instanceof ExpressionConstraint) {
            for (final Expression expression : ((ExpressionConstraint) constraint).getExpressionSubParts()) {
                this.replaceFeatureInExpression(expression);
            }
        } else {
            for (final Constraint subConstraint : constraint.getConstraintSubParts()) {
                this.convertFeaturesInExpressionConstraint(subConstraint);
            }
        }
    }

    private void replaceFeatureInExpression(final Expression expression) {
        if (expression instanceof LiteralExpression) {
            if (((LiteralExpression) expression).getContent() instanceof Attribute<?>) {
                Feature relevantFeature = (Feature) ((Attribute<?>) ((LiteralExpression) expression).getContent()).getFeature();
                expression.replaceExpressionSubPart(expression, new LiteralExpression(new Attribute<Long>(Constants.TYPE_LEVEL_VALUE, 0L, relevantFeature))
                );
            }
        }
        for (final Expression expr : expression.getExpressionSubParts()) {
            this.replaceFeatureInExpression(expr);
        }
    }

    private void traverseFeatures(final Feature feature) {
        if (feature.getFeatureType() != null) {
            feature.getAttributes().put(
                Constants.FEATURE_TYPE,
                new Attribute<>(Constants.FEATURE_TYPE, feature.getFeatureType().getName(), feature)
            );
            feature.setFeatureType(null);
        }

        for (final Group group : feature.getChildren()) {
            for (final Feature subFeature : group.getFeatures()) {
                this.traverseFeatures(subFeature);
            }
        }
    }
}
