event_stream_factory = EventStreamFactory(port=8099)
 
config_provider = CachingConfigurationProvider(
    ContextConfigurationProvider(
        event_stream_factory.context_source(
            source=str(uvl_path.resolve()),
            context_source=WaterTankContextSource(),
        ),
        interface,
    ),
)