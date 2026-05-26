class BPConfigurator(SimpleBProgramRunnerListener):
    def __init__(
            ...,
            extensions: Iterable[BPConfiguratorExtension] | None = None,
    ) -> None:
        ...
        self.__extensions = tuple(extensions or ())