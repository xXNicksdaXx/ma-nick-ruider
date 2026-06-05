def __events_endpoint(self) -> Response:
    @stream_with_context
    def generate():
        yield ": connected\n\n"
        while True:
            try:
                payload = self.__events.get(timeout=15)
                yield _format_sse(payload)
            except Empty:
                yield ": keep-alive\n\n"
    response = Response(generate(), mimetype="text/event-stream")
    ...
    return response