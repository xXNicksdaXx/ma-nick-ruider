public class UVLDiagramModule extends DiagramModule {
    @Override
    protected Class<? extends SourceModelStorage> bindSourceModelStorage() {
        return UVLSourceModelStorage.class;
    }

    @Override
    protected Class<? extends UVLModelState> bindGModelState() {
        return UVLModelStateImpl.class;
    }

    @Override
    protected void configureOperationHandlers(final MultiBinding<OperationHandler<?>> binding) {
        binding.add(UVLApplyLabelEditOperationHandler.class);
        binding.add(UVLDeleteOperationHandler.class);
        binding.add(UVLCreateFeatureOperationHandler.class);
        binding.add(UVLCreateAttributeOperationHandler.class);
        binding.add(UVLCreateRelationEdgeOperationHandler.class);
        ...
    }
    ... 
}