class BEventStreamerExtension(BPConfiguratorExtension):
    ...
    def starting(self, b_program: BProgram) -> None:
        self.__event_stream_server.start()
 
    def event_selected(self, b_program: BProgram, event: BEvent) -> None:
        ...
        for ticket in b_program.tickets:
            request: BEvent | None = ticket.get("request")
            block: BEvent | None = ticket.get("block")
            wait_for: BEvent | None = ticket.get("waitFor")
            thread_name: str = b_program.get_name(ticket["bt"])
            
            if request  and request.name  == event.name:
                self.__publish_event("requested",  thread_name, event.name)
            if block    and block.name    == event.name:
                self.__publish_event("blocked",    thread_name, event.name)
            if wait_for and wait_for.name == event.name:
                self.__publish_event("waited_for", thread_name, event.name)
 
    def __publish_event(self, kind: str, thread_name: str, event_name: str) -> None:
        self.__event_stream_server.publish({
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "source": self.__source,
            "type": "event_selected",
            "kind": kind,
            "thread": thread_name,
            "event": event_name,
        })