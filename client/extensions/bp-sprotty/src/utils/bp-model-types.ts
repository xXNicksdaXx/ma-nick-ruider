/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/

import { DefaultTypes } from '@eclipse-glsp/client';

/**
 * Defines string constants for all element types used in UVL BP diagrams.
 * <p>
 * For corresponding server-side types, see <code>BPModelTypes.java</code>.
 */
export const BPModelTypes = {
    BP_ENV: `${DefaultTypes.NODE}:env`,
    BP_CONFIG: `${DefaultTypes.NODE}:config`,

    B_THREAD: `${DefaultTypes.NODE}:b-thread`,

    REQUESTED_EVENT: `${DefaultTypes.COMPARTMENT}:requested-event`,
    BLOCKED_EVENT: `${DefaultTypes.COMPARTMENT}:blocked-event`,
    WAITED_FOR_EVENT: `${DefaultTypes.COMPARTMENT}:waited-for-event`,
    EVENT_NAME: `${DefaultTypes.LABEL}:eventName`,
    EVENT_PRIORITY: `${DefaultTypes.LABEL}:eventPriority`,

    REQUESTED: `${DefaultTypes.EDGE}:requested`,
    BLOCKED: `${DefaultTypes.EDGE}:blocked`,
    WAITED_FOR: `${DefaultTypes.EDGE}:waited-for`,
    SELECTED: `${DefaultTypes.EDGE}:selected`,
    CONFLICTING: `${DefaultTypes.EDGE}:conflicting`
} as const;
