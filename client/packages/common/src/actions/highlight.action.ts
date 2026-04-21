/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/

import { Action } from '@eclipse-glsp/protocol';

export interface HighlightElementAction extends Action {
    kind: typeof HighlightElementAction.KIND;
    elementIds: string[];
    isHighlighted: boolean;
}

export const HighlightElementAction = {
    KIND: 'highlightElement',

    is(obj: unknown): obj is HighlightElementAction {
        return Action.hasKind(obj, HighlightElementAction.KIND);
    },

    create(
        opts: { elementIds: string[]; isHighlighted: boolean } | { elementId: string; isHighlighted: boolean }
    ): HighlightElementAction {
        const elementIds = 'elementIds' in opts ? opts.elementIds : [opts.elementId];
        return {
            kind: HighlightElementAction.KIND,
            elementIds,
            isHighlighted: opts.isHighlighted
        };
    }
} as const;

