@Inject
protected void registerDataListener() {
    serverSentEventsService.addDataListener(this::updateContextEnv, CONTEXT_UPDATE_TYPE);
}

protected void updateContextEnv(final ParsedServerSentEvent event) {
    Feature envFeature = modelState.getFeatureModel().getEnv();
    ...
    for (Map.Entry<String, ?> entry : event.data().entrySet()) {
        if (envFeature.getAttributes().containsKey(entry.getKey())) {
            Attribute attribute = envFeature.getAttributes().get(entry.getKey());
            if (!Objects.equals(attribute.getValue(), entry.getValue())) {
                attribute.setValue(entry.getValue());
                hasChanges = true;
            }
        }
    }

    if (hasChanges) actionDispatcher.dispatchAll(submitModel(envFeature));
}