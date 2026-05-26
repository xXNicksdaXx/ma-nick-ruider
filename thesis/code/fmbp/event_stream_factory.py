class EventStreamFactory:
    def __init__(self, port: int = 8099) -> None:
        self.__server = EventStreamServer(port=port)
 
    def b_event_stream(self, source: str) -> BEventStreamerExtension:
        return BEventStreamerExtension(source, self.__server)
 
    def context_source(
            self, source: str, context_source: ContextSource
    ) -> ContextSourceStreamDecorator:
        return ContextSourceStreamDecorator(source, context_source, self.__server)