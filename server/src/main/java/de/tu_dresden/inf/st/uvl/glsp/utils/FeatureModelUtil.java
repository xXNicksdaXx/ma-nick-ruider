/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.utils;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class FeatureModelUtil {

    public static List<String> getAllFeatureEdges(Feature feature) {
        List<String> featureEdges = new ArrayList<>();

        Group parentGroup = feature.getParentGroup();
        if (parentGroup != null) {
            String edgeName = GroupUtil.getEdgeName(parentGroup, feature);
            featureEdges.add(edgeName);
        }

        for (Group group : feature.getChildren()) {
            for (Feature childFeature : group.getFeatures()) {
                String childEdgeName = GroupUtil.getEdgeName(group, childFeature);
                featureEdges.add(childEdgeName);
            }
        }

        return featureEdges;
    }

    public static Collection<Group> getAllGroups(FeatureModel featureModel) {
        return featureModel.getFeatureMap().values().stream()
                .map(Feature::getParentGroup)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
