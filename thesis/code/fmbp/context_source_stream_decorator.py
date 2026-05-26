class ContextSourceStreamDecorator(ContextSource):
    def __init__(
            self,
            source: str,
            context_source: ContextSource,
            event_stream_server: EventStreamServer,
    ) -> None:
        self.__source = source
        self.__context_source = context_source
        self.__event_stream_server = event_stream_server
 
    def get_data(self) -> CONTEXT_DATA:
        data = self.__context_source.get_data()
        self.__event_stream_server.publish({
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "source": self.__source,
            "type": "context_update",
            "data": data,
        })
        return data