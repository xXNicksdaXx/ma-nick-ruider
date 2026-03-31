/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import { RectangularNodeView, RenderingContext } from '@eclipse-glsp/client';
import { injectable } from 'inversify';
import { VNode } from 'snabbdom';
import { svg } from 'sprotty';

import { ConstraintBoxNode, FeatureNode, LabeledNode } from "./model";

// Workaround for typing issues: Creating elements returns a `JSX.Element` instead of a `VNode`, which causes compilation errors.
// Therefore, type the VNode elements as `any` to avoid these problems.
// See also: https://github.com/eclipse/sprotty/issues/178
// The solutions described there did not work in this case.
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const JSX = { createElement: svg };

@injectable()
export abstract class SeparatorNodeView<T extends LabeledNode> extends RectangularNodeView {

    override render(node: T, context: RenderingContext): VNode | undefined {
        if (!this.isVisible(node, context)) {
            return undefined;
        }

        return (
            <g>
                <rect
                    x={0}
                    y={0}
                    width={Math.max(0, node.bounds.width)}
                    height={Math.max(0, node.bounds.height)}
                    class-sprotty-node={true}
                    class-selected={node.selected}
                    class-mouseover={node.hoverFeedback}
                />
                {this.renderSeparatorLines(node)}
                {context.renderChildren(node)}
            </g>
        );
    }

    protected abstract renderSeparatorLines(node: T): VNode | undefined;

    protected createSeparatorLine(height: number, width: number): VNode {
        return (
            <line
                x1={0}
                y1={Math.max(0, height)}
                x2={Math.max(0, width)}
                y2={Math.max(0, height)}
                class-separator-line={true}
            />
        );
    }
}

@injectable()
export class FeatureNodeView extends SeparatorNodeView<FeatureNode> {

    renderSeparatorLines(node: FeatureNode): VNode | undefined {
        return (
            node.hasAttributes()
                ? this.createSeparatorLine(node.headerContainer.bounds.height, node.bounds.width)
                : undefined
        )
    }
}

@injectable()
export class ConstraintBoxNodeView extends SeparatorNodeView<ConstraintBoxNode> {

    renderSeparatorLines(node: ConstraintBoxNode): VNode | undefined {
        return (
            node.hasConstraints()
                ? this.createSeparatorLine(node.headerContainer.bounds.height, node.bounds.width)
                : undefined
        )
    }
}
