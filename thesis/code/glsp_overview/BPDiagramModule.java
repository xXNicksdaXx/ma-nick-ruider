public class BPDiagramModule extends UVLDiagramModule {
    @Override
    protected Class<? extends SourceModelStorage> bindSourceModelStorage() {
        return BPSourceModelStorage.class;
    }

    @Override
    protected Class<? extends BPModelState> bindGModelState() {
        return BPModelStateImpl.class;
    }

    @Override
    protected void configureOperationHandlers(final MultiBinding<OperationHandler<?>> binding) {
        super.configureOperationHandlers(binding);

        binding.rebind(UVLApplyLabelEditOperationHandler.class, BPApplyLabelEditOperationHandler.class);
        binding.rebind(UVLDeleteOperationHandler.class, BPDeleteOperationHandler.class);
        binding.add(BPCreateBThreadOperationHandler.class);
        binding.add(BPCreateEventOperationHandler.class);
    }
    ...
}