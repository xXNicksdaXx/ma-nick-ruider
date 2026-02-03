/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.utils;

import de.vill.main.UVLModelFactory;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;

import java.util.List;
import java.util.Optional;

public class FeatureModelEditUtil {

    public static FeatureModel addFeature(FeatureModel featureModel, Feature feature, Optional<Feature> parentFeatureOpt) {
        if (parentFeatureOpt.isEmpty()) {
            featureModel.setRootFeature(feature);
            return reparseFeatureModel(featureModel.toString());
        }

        Feature parentFeature = parentFeatureOpt.get();
        String parentFeatureString = parentFeature.toString();

        List<Group> children = parentFeature.getChildren();
        if (children.isEmpty()) {
            Group newGroup = new Group(Group.GroupType.OPTIONAL);
            newGroup.setParentFeature(parentFeature);
            newGroup.getFeatures().add(feature);
            parentFeature.getChildren().add(newGroup);
        } else {
            Group firstGroup = children.getFirst();
            firstGroup.getFeatures().add(feature);
        }

        String newParentFeatureString = parentFeature.toString();

        String featureModelText = featureModel.toString();
        String updatedText = featureModelText.replaceFirst(parentFeatureString, newParentFeatureString);
        return reparseFeatureModel(updatedText);
    }

    public static FeatureModel renameFeature(FeatureModel featureModel, Feature feature, String newName) {
        String featureModelText = featureModel.toString();

        String oldName = feature.getFeatureName();
        String updatedText = featureModelText.replaceFirst(oldName + "\n", newName + "\n");

        return reparseFeatureModel(updatedText);
    }

    private static FeatureModel reparseFeatureModel(String featureModelText) {
        UVLModelFactory uvlModelFactory = new UVLModelFactory();
        return uvlModelFactory.parse(featureModelText);
    }

}
