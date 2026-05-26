@Override
public void updateRoot(final GModelRoot newRoot) {
    setRoot(newRoot);
    this.index = getOrUpdateIndex(newRoot);
    if (getFeatureModel() != null) {
        getIndex().indexFeatureModel(getFeatureModel());
    }
}

public Optional<UVLObject> getUVLObject(final String id) {
    return Optional.ofNullable(uvlIndex.get(id));
}