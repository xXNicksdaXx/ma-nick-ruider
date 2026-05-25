/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class FMBPEventListenerServiceTest {

  @Test
  void givenEventTypesWithWhitespace_whenNormalizeEventTypes_thenReturnsTrimmedUniqueSet() {
    // Arrange
    FMBPEventListenerService service = new FMBPEventListenerService();

    // Act
    Set<String> normalized =
        service.normalizeEventTypes(" requested ", null, " ", "blocked", "requested");

    // Assert
    assertEquals(Set.of("requested", "blocked"), normalized);
  }

  @Test
  void givenValidJsonPayload_whenParsePayload_thenReturnsParsedEvent() {
    // Arrange
    FMBPEventListenerService service = new FMBPEventListenerService();
    String payload = "{\"type\":\"requested\",\"data\":{\"thread\":\"T1\"}}";

    // Act
    Optional<ParsedServerSentEvent> result = service.parsePayload(payload);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(Optional.of("requested"), result.get().type());
    assertEquals("T1", result.get().data().get("thread"));
  }

  @Test
  void givenInvalidJsonPayload_whenParsePayload_thenReturnsEmptyOptional() {
    // Arrange
    FMBPEventListenerService service = new FMBPEventListenerService();
    String payload = "{not-json";

    // Act
    Optional<ParsedServerSentEvent> result = service.parsePayload(payload);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  void
      givenBufferedPayloadWithMatchingListeners_whenDispatchBufferedData_thenNotifiesOnlyMatches() {
    // Arrange
    FMBPEventListenerService service = new FMBPEventListenerService();
    Consumer<ParsedServerSentEvent> requestedListener = mock(Consumer.class);
    Consumer<ParsedServerSentEvent> blockedListener = mock(Consumer.class);
    Consumer<ParsedServerSentEvent> anyListener = mock(Consumer.class);
    service.addDataListener(requestedListener, "requested");
    service.addDataListener(blockedListener, "blocked");
    service.addDataListener(anyListener);
    StringBuilder buffer = new StringBuilder("{\"type\":\"requested\",\"data\":{\"value\":1}}");

    // Act
    service.dispatchBufferedData(buffer);

    // Assert
    verify(requestedListener, times(1)).accept(any());
    verify(blockedListener, never()).accept(any());
    verify(anyListener, times(1)).accept(any());
    assertEquals(0, buffer.length());
  }

  @Test
  void givenListenerThrowsException_whenDispatchBufferedData_thenContinuesToNextListener() {
    // Arrange
    FMBPEventListenerService service = new FMBPEventListenerService();
    Consumer<ParsedServerSentEvent> failingListener = mock(Consumer.class);
    Consumer<ParsedServerSentEvent> succeedingListener = mock(Consumer.class);
    doThrow(new RuntimeException("boom")).when(failingListener).accept(any());
    service.addDataListener(failingListener, "requested");
    service.addDataListener(succeedingListener, "requested");
    StringBuilder buffer = new StringBuilder("{\"type\":\"requested\"}");

    // Act
    service.dispatchBufferedData(buffer);

    // Assert
    verify(failingListener, times(1)).accept(any());
    verify(succeedingListener, times(1)).accept(any());
  }
}
