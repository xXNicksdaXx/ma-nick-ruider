public class BPFeatureModel extends FeatureModel {
    private Feature env;
    private Feature config;

    public Feature getEnv() { return env; }
    public void setEnv(Feature env) { this.env = env; }

    public Feature getConfig() { return config; }
    public void setConfig(Feature config) { this.config = config; }

    @Override
    protected void appendAdditionalTopLevelFeatures(StringBuilder result, boolean withSubmodels, String currentAlias) {
        if (env != null) result.append(Util.indentEachLine(env.toString(withSubmodels, currentAlias)));
        if (config != null) result.append(Util.indentEachLine(config.toString(withSubmodels, currentAlias)));
    }
}