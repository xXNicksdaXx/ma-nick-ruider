public record ParsedServerSentEvent(
    String rawPayload,
    Map<String, Object> payload,
    Optional<String> type,
    Optional<String> source,
    Optional<Instant> timestamp,
    Map<String, Object> data) {

    public boolean hasType(final String... eventTypes) {
        if (eventTypes == null || eventTypes.length == 0) {
            return true;
        }
        return type.map(current -> ...).orElse(false);
    }
}