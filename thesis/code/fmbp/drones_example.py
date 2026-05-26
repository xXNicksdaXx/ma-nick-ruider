event_stream_factory = EventStreamFactory(port=8099)
 
for i in range(4):
    uvl_path = workspace_path / "drones" / f"drone_{i}.uvl"
 
    config_provider = CachingConfigurationProvider(
        ContextConfigurationProvider(
            event_stream_factory.context_source(
                source=str(uvl_path.resolve()),
                context_source=DroneContextSource(),
            ),
            interface,
        ),
    )
 
    b_program = FMBProgram(
        ...
        listener=BPConfigurator(
            DroneListener(8000 + i, queue),
            ...
            extensions=[event_stream_factory.b_event_stream(
                source=str(uvl_path.resolve()),
            )],
        ),
    )
    process = DroneProcess(b_program)
    process.start()