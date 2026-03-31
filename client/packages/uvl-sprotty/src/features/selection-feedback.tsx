/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import { GChildElement, GModelElement, GShapeElement, IVNodePostprocessor } from '@eclipse-glsp/client';
import { injectable } from 'inversify';
import { VNode } from 'snabbdom';
import { svg } from 'sprotty';

import { EditableGCompartment } from '../model';

// Workaround for typing issues: Creating elements returns a `JSX.Element` instead of a `VNode`, which causes compilation errors.
// Therefore, type the VNode elements as `any` to avoid these problems.
// See also: https://github.com/eclipse/sprotty/issues/178
// The solutions described there did not work in this case.
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const JSX = { createElement: svg };

@injectable()
export class EditableCompartmentSelectionFeedback implements IVNodePostprocessor {
    decorate(vnode: VNode, element: GModelElement): VNode {
        if (element instanceof EditableGCompartment && (element.hoverFeedback || element.selected)) {
            // get the width of the parent element, if possible
            let parent = element.parent;
            if (parent instanceof GChildElement) {
                parent = parent.parent;
            }

            let width: number;
            if (parent && parent instanceof GShapeElement) {
                width = parent.bounds.width;
            } else {
                // if the parent is not available, use the width of the compartment itself
                width = element.bounds.width;
            }

            const feedback: VNode = (
                <rect
                    x={0}
                    y={0}
                    width={width}
                    height={element.bounds.height}
                    class-selection-feedback={true}
                    class-hover={element.hoverFeedback}
                    class-selected={element.selected}
                />
            );
            if (!vnode.children) {
                vnode.children = [];
            }
            vnode.children.push(feedback);
        }
        return vnode;
    }

    postUpdate(): void {
        // nothing to do
    }
}
