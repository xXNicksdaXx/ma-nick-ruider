/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp;

import org.eclipse.glsp.graph.DefaultTypes;

/**
 * Defines string constants for all element types used in UVL diagrams.
 * <p>
 * For corresponding client-side types, see <code>uvl-model-types.ts</code>.
 * <p>
 * This class is not intended to be instantiated.
 */
public final class UVLModelTypes {

    public static final String FEATURE = DefaultTypes.NODE + ":feature";
    public static final String FEATURE_NAME = DefaultTypes.LABEL + ":feature";

    public static final String ATTRIBUTE = DefaultTypes.COMPARTMENT + ":attribute";
    public static final String ATTRIBUTE_NAME = DefaultTypes.LABEL + ":attributeName";
    public static final String ATTRIBUTE_VALUE = DefaultTypes.LABEL + ":attributeValue";

    public static final String MANDATORY = DefaultTypes.EDGE + ":mandatory";
    public static final String OPTIONAL = DefaultTypes.EDGE + ":optional";
    public static final String ALTERNATIVE = DefaultTypes.EDGE + ":alternative";
    public static final String GROUP_CARDINALITY = DefaultTypes.EDGE + ":groupCardinality";
    public static final String OR = DefaultTypes.EDGE + ":or";

    public static final String EQUIVALENCE = DefaultTypes.EDGE + ":equivalence";
    public static final String IMPLICATION = DefaultTypes.EDGE + ":implication";

    public static final String CARDINALITY_LABEL = DefaultTypes.LABEL + ":cardinality";
    public static final String CONSTRAINT_LABEL = DefaultTypes.LABEL + ":constraint";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private UVLModelTypes() {
        // Prevent instantiation
    }
}
