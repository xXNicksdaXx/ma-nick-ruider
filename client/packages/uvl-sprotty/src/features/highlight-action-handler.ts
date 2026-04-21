/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/

import { HighlightElementAction } from 'uvl-common';

import {
    Action,
    Command,
    type CommandExecutionContext,
    type CommandReturn,
    HandleActionResult,
    IActionHandler,
    ICommandStack,
    MaybePromise,
    TYPES
} from '@eclipse-glsp/client';
import { inject, injectable } from 'inversify';

const HIGHLIGHT_CLASS = 'highlight-glow';

const activeHighlightsByRoot = new Map<string, Set<string>>();

@injectable()
export class HighlightElementsActionHandler implements IActionHandler {
    @inject(TYPES.ICommandStack)
    protected commandStack: ICommandStack;

    handle(action: Action): MaybePromise<HandleActionResult> {
        if (!HighlightElementAction.is(action)) {
            return undefined;
        }

        const command = new ApplyHighlightCommand(action.elementIds, action.isHighlighted);
        return this.commandStack.execute(command).then(() => undefined);
    }
}

class ApplyHighlightCommand extends Command {
    constructor(
        protected readonly elementIds: ReadonlyArray<string>,
        protected readonly highlighted: boolean
    ) {
        super();
    }

    execute(context: CommandExecutionContext): CommandReturn {
        return applyHighlight(context, this.elementIds, this.highlighted);
    }

    undo(context: CommandExecutionContext): CommandReturn {
        return context.root;
    }

    redo(context: CommandExecutionContext): CommandReturn {
        return this.execute(context);
    }
}

function applyHighlight(
    context: CommandExecutionContext,
    elementIds: ReadonlyArray<string>,
    highlighted: boolean
): CommandReturn {
    let modelChanged = false;
    const activeHighlights = getActiveHighlights(context);

    if (highlighted) {
        const nextHighlightedIds = new Set<string>();

        for (const rawId of elementIds) {
            const id = rawId?.trim();
            if (!id) {
                continue;
            }

            const element = context.root.index.getById(id);
            if (!element) {
                continue;
            }

            nextHighlightedIds.add(id);
        }

        if (setsEqual(activeHighlights, nextHighlightedIds)) {
            return { model: context.root, modelChanged: false };
        }

        for (const activeId of activeHighlights) {
            const activeElement = context.root.index.getById(activeId);
            if (!activeElement) {
                continue;
            }

            const nextClasses = toggleHighlightClass(activeElement.cssClasses, false);
            if (!classesChanged(activeElement.cssClasses, nextClasses)) {
                continue;
            }

            activeElement.cssClasses = nextClasses;
            modelChanged = true;
        }

        activeHighlights.clear();

        for (const id of nextHighlightedIds) {
            const element = context.root.index.getById(id);
            if (!element) {
                continue;
            }

            const nextClasses = toggleHighlightClass(element.cssClasses, true);
            if (!classesChanged(element.cssClasses, nextClasses)) {
                activeHighlights.add(id);
                continue;
            }

            element.cssClasses = nextClasses;
            activeHighlights.add(id);
            modelChanged = true;
        }
    } else {
        for (const rawId of elementIds) {
            const id = rawId?.trim();
            if (!id) {
                continue;
            }

            const element = context.root.index.getById(id);
            activeHighlights.delete(id);

            if (!element) {
                continue;
            }

            const nextClasses = toggleHighlightClass(element.cssClasses, false);
            if (!classesChanged(element.cssClasses, nextClasses)) {
                continue;
            }

            element.cssClasses = nextClasses;
            modelChanged = true;
        }
    }

    return { model: context.root, modelChanged };
}

function getActiveHighlights(context: CommandExecutionContext): Set<string> {
    const rootId = (context.root as { id?: string }).id ?? 'root';
    let activeHighlights = activeHighlightsByRoot.get(rootId);
    if (!activeHighlights) {
        activeHighlights = new Set<string>();
        activeHighlightsByRoot.set(rootId, activeHighlights);
    }

    return activeHighlights;
}

function classesChanged(previousClasses: ReadonlyArray<string> | undefined, nextClasses: ReadonlyArray<string>): boolean {
    const currentClasses = previousClasses ?? [];
    return currentClasses.length !== nextClasses.length
        || currentClasses.some((cssClass, index) => cssClass !== nextClasses[index]);
}

function setsEqual(left: ReadonlySet<string>, right: ReadonlySet<string>): boolean {
    if (left.size !== right.size) {
        return false;
    }

    for (const value of left) {
        if (!right.has(value)) {
            return false;
        }
    }

    return true;
}

function toggleHighlightClass(cssClasses: ReadonlyArray<string> | undefined, highlighted: boolean): string[] {
    const classSet = new Set(cssClasses ?? []);

    if (highlighted) {
        classSet.add(HIGHLIGHT_CLASS);
    } else {
        classSet.delete(HIGHLIGHT_CLASS);
    }

    return [...classSet];
}
