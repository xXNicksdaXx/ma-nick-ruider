class EventStreamServer:
    def __init__(self, port: int = 8099) -> None:
        self.__port = port
        self.__app = Flask("fmbp-event-stream")
        self.__events: Queue[str] = Queue(maxsize=10000)
        self.__started = False
        self.__flask_task = Thread(
            target=self.__app.run,
            kwargs={"port": self.__port, "threaded": True},
            daemon=True,
        )
        self.__app.route("/events")(self.__events_endpoint)
        self.__app.route("/health")(self.__health_endpoint)