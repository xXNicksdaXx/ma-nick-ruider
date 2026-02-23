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
export namespace UVLModelTypes {
    export const FEATURE = `${DefaultTypes.NODE}:feature`;
    export const FEATURE_NAME = `${DefaultTypes.LABEL}:feature`;

    export const ATTRIBUTE = `${DefaultTypes.COMPARTMENT}:attribute`;
    export const ATTRIBUTE_NAME = `${DefaultTypes.LABEL}:attributeName`;
    export const ATTRIBUTE_VALUE = `${DefaultTypes.LABEL}:attributeValue`;

    export const MANDATORY = `${DefaultTypes.EDGE}:mandatory`;
    export const OPTIONAL = `${DefaultTypes.EDGE}:optional`;
    export const ALTERNATIVE = `${DefaultTypes.EDGE}:alternative`;
    export const GROUP_CARDINALITY = `${DefaultTypes.EDGE}:groupCardinality`;
    export const OR = `${DefaultTypes.EDGE}:or`;

    export const EQUIVALENCE = `${DefaultTypes.EDGE}:equivalence`;
    export const IMPLICATION = `${DefaultTypes.EDGE}:implication`;

    export const CARDINALITY_LABEL = `${DefaultTypes.LABEL}:cardinality`;
    export const CONSTRAINT_LABEL = `${DefaultTypes.LABEL}:constraint`;
}
