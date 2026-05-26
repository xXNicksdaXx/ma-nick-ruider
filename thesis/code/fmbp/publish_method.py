def publish(self, payload: dict[str, Any]) -> None:
    if not self.__started:
        return
    serialized = json.dumps(payload, default=str)
    try:
        self.__events.put_nowait(serialized)
    except Full:
        try:
            self.__events.get_nowait()
            self.__events.put_nowait(serialized)
        except Empty:
            pass