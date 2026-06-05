@Inject
protected void registerDataListener() {
    serverSentEventsService.addDataListener(this::dispatchHighlightAction, "requested", "blocked", "waited_for");
}

protected void dispatchHighlightAction(final ParsedServerSentEvent event) {
    Optional<String> threadName = parseThreadName(event);
    Optional<String> eventName  = parseEventName(event);

    Set<String> matchingFeatureIds = eventName.isPresent()
        ? resolveFeatureIdsByEventName(threadName.get(), eventName.get())
        : resolveFeatureIdsByThreadName(threadName.get());

    actionDispatcher.dispatch(
        new HighlightElementAction(List.copyOf(matchingFeatureIds), true));
}