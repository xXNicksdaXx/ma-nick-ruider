def __call_extensions_starting(self, b_program: BProgram) -> None:
    for extension in self.__extensions:
        try:
            extension.starting(b_program)
        except Exception:
            logging.exception("Extension failed during starting")
 
def __call_extensions_event_selected(self, b_program: BProgram,
                                     event: BEvent) -> None:
    for extension in self.__extensions:
        try:
            extension.event_selected(b_program, event)
        except Exception:
            logging.exception("Extension failed during event_selected")