/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ParsedServerSentEventTest {

  @Test
  void givenPayloadWithMetadata_whenFrom_thenNormalizesAndExtractsFields() {
    // Arrange
    Map<Object, Object> rawPayload = new LinkedHashMap<>();
    rawPayload.put("type", " requested ");
    rawPayload.put("source", " file:/tmp/test ");
    rawPayload.put("timestamp", "2024-01-01T12:34:56Z");
    Map<Object, Object> data = new LinkedHashMap<>();
    data.put("value", 7);
    data.put(1, "ignored");
    rawPayload.put("data", data);
    rawPayload.put(123, "ignored");

    // Act
    ParsedServerSentEvent event = ParsedServerSentEvent.from("raw", rawPayload);

    // Assert
    assertEquals(Optional.of("requested"), event.type());
    assertEquals(Optional.of("file:/tmp/test"), event.source());
    assertEquals(Optional.of(Instant.parse("2024-01-01T12:34:56Z")), event.timestamp());
    assertFalse(event.payload().containsKey(123));
    assertEquals(1, event.data().size());
    assertEquals(7, event.data().get("value"));
  }

  @Test
  void givenEventWithType_whenHasTypeCalledWithMatchingAndNonMatchingValues_thenRespectsMatches() {
    // Arrange
    ParsedServerSentEvent event = ParsedServerSentEvent.from("raw", Map.of("type", "requested"));

    // Act
    boolean matches = event.hasType("blocked", "requested");
    boolean mismatches = event.hasType("blocked");

    // Assert
    assertTrue(matches);
    assertFalse(mismatches);
  }

  @Test
  void givenEventWithoutType_whenHasTypeCalledWithNoTypes_thenReturnsTrue() {
    // Arrange
    ParsedServerSentEvent event = ParsedServerSentEvent.from("raw", Map.of());

    // Act
    boolean result = event.hasType();

    // Assert
    assertTrue(result);
  }

  @Test
  void givenPayloadWithInvalidTimestamp_whenFrom_thenTimestampIsEmpty() {
    // Arrange
    Map<String, Object> payload = Map.of("timestamp", "not-a-date");

    // Act
    ParsedServerSentEvent event = ParsedServerSentEvent.from("raw", payload);

    // Assert
    assertTrue(event.timestamp().isEmpty());
  }
}
