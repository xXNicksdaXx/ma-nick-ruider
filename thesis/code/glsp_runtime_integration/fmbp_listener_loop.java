@Override
public synchronized void start() {
    ...
    if (executor == null || executor.isShutdown()) {
        executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "bp-sse-listener"));
    }
    streamTask = CompletableFuture.runAsync(this::runStreamLoop, executor);
}

protected void runStreamLoop() {
    while (running.get()) {
    waitForHealthyEndpoint();
    HttpRequest request = HttpRequest.newBuilder(resolveEventsEndpoint())
        .header("Accept", "text/event-stream")
        .GET()
        .build();
    HttpResponse<Stream<String>> response =
        httpClient.send(request, HttpResponse.BodyHandlers.ofLines());
    consumeResponse(response.body());
    }
}