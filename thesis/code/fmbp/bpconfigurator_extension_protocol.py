class BPConfiguratorExtension(Protocol):
    def starting(self, b_program: BProgram) -> None:
        ...
 
    def event_selected(self, b_program: BProgram,
                       event: BEvent) -> None:
        ...