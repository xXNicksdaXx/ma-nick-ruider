package de.tu_dresden.inf.st.uvl.bp.metamodel.conversion;

import de.tu_dresden.inf.st.uvl.bp.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.Group;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.LanguageLevel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DropFeatureCardinality implements IConversionStrategy {
    @Override
    public Set<LanguageLevel> getLevelsToBeRemoved() {
        return new HashSet<>(Arrays.asList(LanguageLevel.FEATURE_CARDINALITY));
    }

    @Override
    public Set<LanguageLevel> getTargetLevelsOfConversion() {
        return new HashSet<>();
    }

    @Override
    public void convertFeatureModel(FeatureModel rootFeatureModel, FeatureModel featureModel) {
        removeFeatureCardinalityRecursively(featureModel.getRootFeature());
    }

    private void removeFeatureCardinalityRecursively(Feature feature) {
        feature.setCardinality(null);
        for (Group group : feature.getChildren()) {
            for (Feature childFeature : group.getFeatures()) {
                //stop when feature is submodelroot to only consider this featuremodel and no submodels
                if (!feature.isSubmodelRoot()) {
                    removeFeatureCardinalityRecursively(childFeature);
                }
            }
        }
    }
}
