public interface UVLModelState extends GModelState {
    FeatureModel getFeatureModel();

    void setFeatureModel(FeatureModel model);

    void updateIndex();

    @Override
    UVLModelIndex getIndex();
}