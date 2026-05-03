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
    IModelIndex,
    MaybePromise,
    TYPES
} from '@eclipse-glsp/client';
import { inject, injectable } from 'inversify';

const HIGHLIGHT_CLASS = 'highlight-glow';
const HIGHLIGHT_TIMEOUT_MS = 10000; // 10 seconds

interface HighlightState {
    active: Set<string>;
    timeoutId: ReturnType<typeof setTimeout> | null;
    mostRecentId: string | null;
}

const activeHighlightsByRoot = new WeakMap<object, HighlightState>();

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
    const root = context.root;
    const index = root.index;
    const state = getActiveHighlightsState(root as object);
    let modelChanged = false;

    if (highlighted) {
        // Get the most recent (last) element ID
        const mostRecentId = elementIds.length > 0 ? elementIds[elementIds.length - 1] : null;

        // Cancel any existing timeout
        if (state.timeoutId !== null) {
            clearTimeout(state.timeoutId);
        }

        // Remove all previous highlights
        for (const activeId of state.active) {
            const activeElement = index.getById(activeId);
            if (activeElement) {
                const nextClasses = updateHighlightClass(activeElement.cssClasses, false);
                if (nextClasses) {
                    activeElement.cssClasses = nextClasses;
                }
                modelChanged = true;
            }
        }

        // Apply highlights to new elements
        for (const id of elementIds) {
            const element = index.getById(id);
            if (element) {
                const nextClasses = updateHighlightClass(element.cssClasses, true);
                if (nextClasses) {
                    element.cssClasses = nextClasses;
                }
                modelChanged = true;
            }
        }

        state.active.clear();
        elementIds.forEach(id => state.active.add(id));

        // Set timeout for the most recent element
        if (mostRecentId) {
            state.mostRecentId = mostRecentId;
            state.timeoutId = setTimeout(() => {
                clearHighlightElement(root, index, state, mostRecentId);
            }, HIGHLIGHT_TIMEOUT_MS);
        }
    } else {
        // Cancel any existing timeout
        if (state.timeoutId !== null) {
            clearTimeout(state.timeoutId);
            state.timeoutId = null;
        }
        state.mostRecentId = null;

        for (const id of elementIds) {
            const element = index.getById(id);
            if (element) {
                const nextClasses = updateHighlightClass(element.cssClasses, false);
                if (nextClasses) {
                    element.cssClasses = nextClasses;
                }
                modelChanged = true;
            }
            state.active.delete(id);
        }
    }

    return { model: root, modelChanged };
}

function getActiveHighlightsState(root: object): HighlightState {
    let state = activeHighlightsByRoot.get(root);
    if (!state) {
        state = {
            active: new Set<string>(),
            timeoutId: null,
            mostRecentId: null
        };
        activeHighlightsByRoot.set(root, state);
    }

    return state;
}

function clearHighlightElement(
    root: object,
    index: IModelIndex,
    state: HighlightState,
    elementId: string
): void {
    const element = index.getById(elementId);
    if (element) {
        const nextClasses = updateHighlightClass(element.cssClasses, false);
        if (nextClasses) {
            element.cssClasses = nextClasses;
        }
    }
    state.active.delete(elementId);
    state.mostRecentId = null;
    state.timeoutId = null;
}

function updateHighlightClass(cssClasses: ReadonlyArray<string> | undefined, highlighted: boolean): string[] | undefined {
    const classes = cssClasses ?? [];
    let hasHighlightClass = false;
    let highlightIndex = -1;

    // Single pass: find highlight class and track position
    for (let i = 0; i < classes.length; i++) {
        if (classes[i] === HIGHLIGHT_CLASS) {
            hasHighlightClass = true;
            highlightIndex = i;
            break;
        }
    }

    // No change needed
    if (highlighted === hasHighlightClass) {
        return undefined;
    }

    // Add highlight class
    if (highlighted) {
        return classes.length === 0 ? [HIGHLIGHT_CLASS] : [...classes, HIGHLIGHT_CLASS];
    }

    // Remove highlight class - build new array without it in one pass
    const nextClasses = new Array<string>(classes.length - 1);
    let writeIdx = 0;
    for (let i = 0; i < classes.length; i++) {
        if (i !== highlightIndex) {
            nextClasses[writeIdx++] = classes[i];
        }
    }

    return nextClasses;
}
