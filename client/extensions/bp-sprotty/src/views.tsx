/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import {
    GEdge,
    GEdgeView,
    Point
} from '@eclipse-glsp/client';
import { injectable } from 'inversify';
import { VNode } from 'snabbdom';
import { svg } from 'sprotty';

import { BThreadNode } from "./model";
import {SeparatorNodeView} from "uvl-sprotty";

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

/**
 * Simple dashed edge view used to render all BP event constraint edges.
 *
 * The dashed stroke visually distinguishes BP event constraints from
 * regular UVL implication / equivalence edges. Add further styling or
 * arrowhead rendering here as the BP feature set grows.
 */
@injectable()
export class BPEdgeView extends GEdgeView {
    protected override renderLine(_edge: GEdge, segments: Point[]): VNode {
        const firstPoint = segments[0];
        let path = `M ${firstPoint.x},${firstPoint.y}`;
        for (let i = 1; i < segments.length; i++) {
            const p = segments[i];
            path += ` L ${p.x},${p.y}`;
        }
        return (
            <path
                class-sprotty-edge={true}
                class-bp-edge={true}
                stroke-dasharray="6,4"
                d={path}
            />
        ) as VNode;
    }
}
