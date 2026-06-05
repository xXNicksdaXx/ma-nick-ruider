public UVLListener(ModelType modelType) {
    FeatureModel featureModel = modelType == ModelType.BP
        ? new BPFeatureModel()
        : new FeatureModel();
    this.fmBuilder = new FeatureModelBuilder(featureModel);
}