public void loadSourceModel(RequestModelAction action) {
    File featureModelFile = convertToFile(action.getOptions());
    validateModelPath(featureModelFile, ClientOptionsUtil.getSourceUri(action.getOptions()));
    loadFeatureModel(featureModelFile);

    File notationFile = getNotationFile(featureModelFile.getAbsolutePath());
    loadGModel(notationFile);
}