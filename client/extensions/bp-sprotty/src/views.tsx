/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import { injectable } from 'inversify';
import { VNode } from 'snabbdom';
import { svg } from 'sprotty';

import { SeparatorNodeView } from "uvl-sprotty";
import { BThreadNode } from "./model";

// Workaround for typing issues with JSX / VNode – see uvl-sprotty views.tsx
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const JSX = { createElement: svg };

@injectable()
export class BThreadNodeView extends SeparatorNodeView<BThreadNode> {

    protected override renderSeparatorLines(node: BThreadNode): VNode | undefined {
        const showAttributes = node.hasAttributes();
        const showEvents = node.hasEvents();

        if (!showAttributes && !showEvents) {
            return undefined;
        }

        return (
            <g>
                {showAttributes
                    ? this.createSeparatorLine(
                        node.headerContainer.bounds.height,
                        node.bounds.width
                    )
                    : undefined}
                {showEvents
                    ? this.createSeparatorLine(
                        node.headerContainer.bounds.height + node.attributeContainer.bounds.height,
                        node.bounds.width
                    )
                    : undefined}
            </g>
        );
    }
}
