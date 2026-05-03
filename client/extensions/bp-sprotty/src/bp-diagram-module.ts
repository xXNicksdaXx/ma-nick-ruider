/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import {
    configureModelElement,
    GCompartmentView,
    GLabelView,
    GModelElement,
    HelperLineType,
    TYPES
} from '@eclipse-glsp/client';
import { ContainerModule } from 'inversify';

import { UVLModelTypes } from "uvl-common";
import { EditableGCompartment, EditableGLabel, FeatureNode, FeatureNodeView } from "uvl-sprotty";

import { BThreadNode } from "./model";
import { BPModelTypes } from './utils/bp-model-types';
import { BThreadNodeView } from './views';

import '../css/diagram.css';

/**
 * An InversifyJS container module that registers all BP-specific diagram elements
 * on top of the base UVL diagram module.
 */
export const bpDiagramModule = new ContainerModule((bind, unbind, isBound, rebind) => {
    const context = { bind, unbind, isBound, rebind };

    rebind(TYPES.IHelperLineOptions).toConstantValue({
        elementLines: [
            HelperLineType.Left, HelperLineType.Center, HelperLineType.Right,
            HelperLineType.Top, HelperLineType.Middle, HelperLineType.Bottom
        ],
        viewportLines: [],
        alignmentElementFilter: (element: GModelElement) =>
               element.type === UVLModelTypes.FEATURE
            || element.type === BPModelTypes.B_THREAD
            || element.type === BPModelTypes.BP_CONFIG
            || element.type === BPModelTypes.BP_ENV,
        minimumMoveDelta: { x: 16, y: 16 },
        alignmentEpsilon: 0.5
    });

    configureModelElement(context, BPModelTypes.BP_CONFIG, FeatureNode, FeatureNodeView);
    configureModelElement(context, BPModelTypes.BP_ENV, FeatureNode, FeatureNodeView);
    configureModelElement(context, BPModelTypes.B_THREAD, BThreadNode, BThreadNodeView);

    configureModelElement(context, BPModelTypes.REQUESTED_EVENT, EditableGCompartment, GCompartmentView);
    configureModelElement(context, BPModelTypes.WAITED_FOR_EVENT, EditableGCompartment, GCompartmentView);
    configureModelElement(context, BPModelTypes.BLOCKED_EVENT, EditableGCompartment, GCompartmentView);

    configureModelElement(context, BPModelTypes.EVENT_NAME, EditableGLabel, GLabelView);
    configureModelElement(context, BPModelTypes.EVENT_PRIORITY, EditableGLabel, GLabelView);
});
