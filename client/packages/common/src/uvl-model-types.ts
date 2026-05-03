/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/

import { DefaultTypes } from '@eclipse-glsp/client';

/**
 * Defines string constants for all element types used in UVL diagrams.
 * <p>
 * For corresponding server-side types, see <code>UVLModelTypes.java</code>.
 */
export const UVLModelTypes = {
    CONSTRAINT_BOX: `${DefaultTypes.NODE}:constraintBox`,
    CONSTRAINT: `${DefaultTypes.COMPARTMENT}:constraint`,
    CONSTRAINT_TEXT: `${DefaultTypes.LABEL}:constraintText`,

    FEATURE: `${DefaultTypes.NODE}:feature`,
    FEATURE_NAME: `${DefaultTypes.LABEL}:feature`,

    ATTRIBUTE: `${DefaultTypes.COMPARTMENT}:attribute`,
    ATTRIBUTE_NAME: `${DefaultTypes.LABEL}:attributeName`,
    ATTRIBUTE_VALUE: `${DefaultTypes.LABEL}:attributeValue`,

    MANDATORY: `${DefaultTypes.EDGE}:mandatory`,
    OPTIONAL: `${DefaultTypes.EDGE}:optional`,
    ALTERNATIVE: `${DefaultTypes.EDGE}:alternative`,
    GROUP_CARDINALITY: `${DefaultTypes.EDGE}:groupCardinality`,
    OR: `${DefaultTypes.EDGE}:or`,

    EXCLUDES: `${DefaultTypes.EDGE}:excludes`,
    REQUIRES: `${DefaultTypes.EDGE}:requires`,

    CARDINALITY_LABEL: `${DefaultTypes.LABEL}:cardinality`,
    CONSTRAINT_EDGE_LABEL: `${DefaultTypes.LABEL}:constraint`
} as const;
