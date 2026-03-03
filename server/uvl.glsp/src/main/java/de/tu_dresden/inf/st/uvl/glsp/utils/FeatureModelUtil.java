/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.utils;

import de.tu_dresden.inf.st.uvl.metamodel.model.Cardinality;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.EquivalenceConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.ImplicationConstraint;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.LiteralConstraint;

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

    public static Collection<Constraint> getEdgeConstraints(FeatureModel featureModel) {
        return featureModel.getOwnConstraints().stream()
                .filter(constraint -> constraint instanceof ImplicationConstraint || constraint instanceof EquivalenceConstraint)
                .filter(constraint -> constraint.getConstraintSubParts().stream()
                        .filter(subConstraint -> subConstraint instanceof LiteralConstraint)
                        .toList().size() == 2)
                .toList();
    }

    public static Collection<Constraint> getComplexConstraints(FeatureModel featureModel) {
        Collection<Constraint> edgeConstraints = getEdgeConstraints(featureModel);
        return featureModel.getOwnConstraints().stream()
                .filter(constraint -> !edgeConstraints.contains(constraint))
                .toList();
    }

    public static boolean includesFeatureCardinality(FeatureModel featureModel) {
        return featureModel.getFeatureMap().values().stream()
                .anyMatch(feature -> feature.getCardinality() != null);
    }

    public static Cardinality createCardinality(String cardinalityString) {
        if (cardinalityString == null || cardinalityString.isEmpty()) {
            return null;
        }
        String[] parts = cardinalityString.split("\\.\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid cardinality format: " + cardinalityString);
        }

        // allow upper bound to be "*" for unbounded cardinality -> Integer.MAX_VALUE
        if (parts[1].equals("*")) {
            parts[1] = String.valueOf(Integer.MAX_VALUE);
        }

        try {
            int lowerBound = Integer.parseInt(parts[0]);
            int upperBound = Integer.parseInt(parts[1]);
            return new Cardinality(lowerBound, upperBound);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cardinality bounds must be integers: " + cardinalityString, e);
        }
    }

    public static String getCardinalityText(Cardinality cardinality) {
        if (cardinality == null) {
            return "";
        }
        String upperBoundText = cardinality.upper == Integer.MAX_VALUE
                ? "*"
                : String.valueOf(cardinality.upper);
        return cardinality.lower + ".." + upperBoundText;
    }
}
