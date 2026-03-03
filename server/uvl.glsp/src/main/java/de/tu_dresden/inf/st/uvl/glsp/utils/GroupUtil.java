/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.utils;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;

public class GroupUtil {

    public static String getGroupName(Group group) {
        String parentName = group.getParentFeature().getFeatureName();
        String groupType = group.GROUPTYPE.toString().toLowerCase();
        return parentName + "_" + groupType;
    }

    public static String getEdgeName(Group group, Feature feature) {
        String baseName = getGroupName(group);
        if (group.getFeatures().contains(feature)) {
            String featureName = feature.getFeatureName();
            return baseName + "_" + featureName;
        }
        return baseName;
    }

    public static String convertGroupTypeToModelType(Group.GroupType groupType) {
        return switch (groupType) {
            case ALTERNATIVE -> UVLModelTypes.ALTERNATIVE;
            case OPTIONAL -> UVLModelTypes.OPTIONAL;
            case MANDATORY -> UVLModelTypes.MANDATORY;
            case GROUP_CARDINALITY ->  UVLModelTypes.GROUP_CARDINALITY;
            case OR -> UVLModelTypes.OR;
        };
    }

    public static Group.GroupType convertModelTypeToGroupType(String modelType) {
        return switch (modelType) {
            case UVLModelTypes.ALTERNATIVE -> Group.GroupType.ALTERNATIVE;
            case UVLModelTypes.OPTIONAL -> Group.GroupType.OPTIONAL;
            case UVLModelTypes.MANDATORY -> Group.GroupType.MANDATORY;
            case UVLModelTypes.GROUP_CARDINALITY -> Group.GroupType.GROUP_CARDINALITY;
            case UVLModelTypes.OR -> Group.GroupType.OR;
            default -> throw new IllegalArgumentException("Unknown model type: " + modelType);
        };
    }
}
