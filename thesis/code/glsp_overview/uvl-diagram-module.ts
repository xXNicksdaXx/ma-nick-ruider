const uvlDiagramModule = new ContainerModule((bind, unbind, isBound, rebind) => {
    const context = {bind, unbind, isBound, rebind};
    ...
    bindAsService(context, TYPES.IAnchorComputer, CenteredAnchor);
    ...
    bind(HighlightElementsActionHandler).toSelf().inSingletonScope();
    configureActionHandler(context, HighlightElementAction.KIND, HighlightElementsActionHandler);

    configureDefaultModelElements(context);
    ...
    configureModelElement(context, UVLModelTypes.FEATURE, FeatureNode, FeatureNodeView);
    configureModelElement(context, UVLModelTypes.ATTRIBUTE, EditableGCompartment, GCompartmentView);
    configureModelElement(context, UVLModelTypes.REQUIRES, GEdge, SingleArrowEdgeView);
    ...
});