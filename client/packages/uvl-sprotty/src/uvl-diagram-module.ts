/****************************************************************************
 *
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 *
 ****************************************************************************/
import {
    bindAsService,
    configureActionHandler,
    configureDefaultModelElements,
    configureModelElement,
    ConsoleLogger,
    ContainerConfiguration,
    defaultModule,
    DefaultTypes,
    GCompartmentView,
    GEdge,
    GEdgeView,
    GLabel,
    GLabelView,
    GLSPPolylineEdgeRouter,
    GModelElement,
    GShapedPreRenderedElement,
    helperLineModule,
    HelperLineType,
    initializeDiagramContainer,
    layoutableChildFeature,
    LogLevel,
    overrideModelElement,
    PreRenderedView,
    TYPES
} from '@eclipse-glsp/client';
import { Container, ContainerModule } from 'inversify';

import { HighlightElementAction, UVLModelTypes } from 'uvl-common';

import 'balloon-css/balloon.min.css';
import '../css/diagram.css';
import '../css/tool-palette.css';

import { CircleEdgeView, DoubleArrowEdgeView, SectorEdgeView, SingleArrowEdgeView } from "./edge-views";
import { CenteredAnchor } from "./features/center-anchor-computer";
import { HighlightElementsActionHandler } from './features/highlight-action-handler';
import { EditableCompartmentSelectionFeedback } from './features/selection-feedback';
import { UVLPolylineEdgeRouter } from "./features/uvl-polyline-edge-router";
import { ConstraintBoxNode, EditableGCompartment, EditableGLabel, FeatureNode } from "./model";
import { ConstraintBoxNodeView, FeatureNodeView } from './node-views';

const uvlDiagramModule = new ContainerModule((bind, unbind, isBound, rebind) => {
    const context = {bind, unbind, isBound, rebind};
    rebind(TYPES.ILogger).to(ConsoleLogger).inSingletonScope();
    rebind(TYPES.LogLevel).toConstantValue(LogLevel.warn);

    bind(TYPES.IVNodePostprocessor).to(EditableCompartmentSelectionFeedback);
    bind(TYPES.IHelperLineOptions).toConstantValue({
        elementLines: [
            HelperLineType.Left, HelperLineType.Center, HelperLineType.Right,
            HelperLineType.Top, HelperLineType.Middle, HelperLineType.Bottom
        ],
        viewportLines: [],
        alignmentElementFilter: (element: GModelElement) => element.type === UVLModelTypes.FEATURE,
        minimumMoveDelta: { x: 16, y: 16 },
        alignmentEpsilon: 0.5
    });

    bindAsService(context, TYPES.IAnchorComputer, CenteredAnchor);
    bind(UVLPolylineEdgeRouter).toSelf().inSingletonScope();
    rebind(GLSPPolylineEdgeRouter).toService(UVLPolylineEdgeRouter);

    bind(HighlightElementsActionHandler).toSelf().inSingletonScope();
    configureActionHandler(context, HighlightElementAction.KIND, HighlightElementsActionHandler);

    configureDefaultModelElements(context);
    overrideModelElement(context, DefaultTypes.SHAPE_PRE_RENDERED, GShapedPreRenderedElement, PreRenderedView, {
        enable: [layoutableChildFeature]
    });

    // Register custom model elements and their views
    configureModelElement(context, UVLModelTypes.CONSTRAINT_BOX, ConstraintBoxNode, ConstraintBoxNodeView);
    configureModelElement(context, UVLModelTypes.CONSTRAINT, EditableGCompartment, GCompartmentView);
    configureModelElement(context, UVLModelTypes.CONSTRAINT_TEXT, EditableGLabel, GLabelView);

    configureModelElement(context, UVLModelTypes.FEATURE, FeatureNode, FeatureNodeView);
    configureModelElement(context, UVLModelTypes.FEATURE_NAME, EditableGLabel, GLabelView);

    configureModelElement(context, UVLModelTypes.ATTRIBUTE, EditableGCompartment, GCompartmentView);
    configureModelElement(context, UVLModelTypes.ATTRIBUTE_NAME, EditableGLabel, GLabelView);
    configureModelElement(context, UVLModelTypes.ATTRIBUTE_VALUE, EditableGLabel, GLabelView);

    configureModelElement(context, UVLModelTypes.MANDATORY, GEdge, CircleEdgeView);
    configureModelElement(context, UVLModelTypes.OPTIONAL, GEdge, CircleEdgeView);
    configureModelElement(context, UVLModelTypes.ALTERNATIVE, GEdge, SectorEdgeView);
    configureModelElement(context, UVLModelTypes.GROUP_CARDINALITY, GEdge, GEdgeView);
    configureModelElement(context, UVLModelTypes.OR, GEdge, SectorEdgeView);

    configureModelElement(context, UVLModelTypes.REQUIRES, GEdge, SingleArrowEdgeView);
    configureModelElement(context, UVLModelTypes.EXCLUDES, GEdge, DoubleArrowEdgeView);

    configureModelElement(context, UVLModelTypes.CARDINALITY_LABEL, EditableGLabel, GLabelView);
    configureModelElement(context, UVLModelTypes.CONSTRAINT_EDGE_LABEL, GLabel, GLabelView)
});

/**
 * Initializes the diagram DI container with the UVL diagram module and all required Sprotty/GLSP modules.
 *
 * @param container The InversifyJS container to configure.
 * @param containerConfiguration Additional container configuration modules.
 * @returns The initialized container.
 */
export function initializeUvlDiagramContainer(container: Container, ...containerConfiguration: ContainerConfiguration): Container {
    return initializeDiagramContainer(container, defaultModule, helperLineModule, uvlDiagramModule, ...containerConfiguration);
}
