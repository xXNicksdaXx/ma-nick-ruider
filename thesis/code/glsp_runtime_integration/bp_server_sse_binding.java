@Override
protected void configureAdditionals() {
    super.configureAdditionals();

    bind(ServerSentEventsService.class).to(FMBPEventListenerService.class).asEagerSingleton();
    bind(FMBPHighlightActionDispatchService.class).asEagerSingleton();
    bind(FMBPContextUpdateService.class).asEagerSingleton();
}