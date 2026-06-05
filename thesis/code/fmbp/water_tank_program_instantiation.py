b_program = FMBProgram(
    bthreads=[add_hot(), add_cold(), remove_water(), finished()],
    event_selection_strategy=PriorityBasedEventSelectionStrategy(),
    listener=BPConfigurator(
        WaterTankListener(TANK),
        config_provider,
        DynamicConsistencyChecker(interface),
        MTimeUpdatingModelWatcher(interface),
        extensions=[event_stream_factory.b_event_stream(
            source=str(uvl_path.resolve()),
        )],
    ),
)
b_program.run()