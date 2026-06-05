@Override
public void enterEnvConfigFeature(UVLJavaParser.EnvConfigFeatureContext ctx) {
    if (rejectIfBaseModel("Env/Config top-level feature", ctx.getStart().getLine())) return;
    String featureName = ctx.reference().getText().replace("\"", "");
    Feature feature = new Feature(featureName);
    featureStack.push(feature);
}

@Override
public void exitEnvConfigFeature(UVLJavaParser.EnvConfigFeatureContext ctx) {
    if (rejectIfBaseModel("Env/Config top-level feature", ctx.getStart().getLine())) return;
    Feature feature = featureStack.pop();
    BPFeatureModel featureModel = (BPFeatureModel) fmBuilder.getFeatureModel();
    if (feature.getFeatureName().equals("Env")) {
        featureModel.setEnv(feature);
    } else if (feature.getFeatureName().equals("Config")) {
        featureModel.setConfig(feature);
    } else {
        errorList.add(new ParseError(
            "Only features named Env and Config are allowed as additional top-level features!"));
    }
    featureModel.getFeatureMap().put(feature.getFeatureName(), feature);
}