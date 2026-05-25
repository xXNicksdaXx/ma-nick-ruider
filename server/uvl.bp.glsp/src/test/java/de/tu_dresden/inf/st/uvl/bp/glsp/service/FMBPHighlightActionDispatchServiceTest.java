/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.tu_dresden.inf.st.uvl.bp.glsp.model.BPModelState;
import de.tu_dresden.inf.st.uvl.glsp.actions.HighlightElementAction;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.BPFeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FMBPHighlightActionDispatchServiceTest {

  @Test
  void givenMatchingSourceThreadAndEvent_whenDispatchHighlightAction_thenDispatchesHighlight() {
    // Arrange
    String threadName = "OrderThread";
    String eventName = "StartEvent";
    Feature bThread = createBThreadWithEvent(threadName, eventName);
    BPFeatureModel featureModel = new BPFeatureModel();
    featureModel.getFeatureMap().put("thread", bThread);

    BPModelState modelState = mock(BPModelState.class);
    UVLModelIndex modelIndex = mock(UVLModelIndex.class);
    when(modelState.getFeatureModel()).thenReturn(featureModel);
    when(modelState.getIndex()).thenReturn(modelIndex);
    when(modelIndex.getIdFor(bThread)).thenReturn(Optional.of("bthread-1"));
    Path sourcePath = Path.of(System.getProperty("java.io.tmpdir"), "model.uvl");
    when(modelState.getClientOptions())
        .thenReturn(Map.of("sourceUri", sourcePath.toUri().toString()));

    ActionDispatcher actionDispatcher = mock(ActionDispatcher.class);

    FMBPHighlightActionDispatchService service = new FMBPHighlightActionDispatchService();
    service.modelState = modelState;
    service.actionDispatcher = actionDispatcher;
    service.serverSentEventsService = mock(ServerSentEventsService.class);

    ParsedServerSentEvent event =
        new ParsedServerSentEvent(
            "raw",
            Map.of("thread", threadName, "event", eventName),
            Optional.of("requested"),
            Optional.of(sourcePath.toString()),
            Optional.empty(),
            Map.of());

    // Act
    service.dispatchHighlightAction(event);

    // Assert
    ArgumentCaptor<Action> captor = ArgumentCaptor.forClass(Action.class);
    verify(actionDispatcher, times(1)).dispatch(captor.capture());
    HighlightElementAction highlightAction = (HighlightElementAction) captor.getValue();
    assertEquals(List.of("bthread-1"), highlightAction.getElementIds());
    assertTrue(highlightAction.isHighlighted());
  }

  @Test
  void givenMismatchedSource_whenDispatchHighlightAction_thenDoesNotDispatch() {
    // Arrange
    BPModelState modelState = mock(BPModelState.class);
    Path expectedSource = Path.of(System.getProperty("java.io.tmpdir"), "expected.uvl");
    when(modelState.getClientOptions())
        .thenReturn(Map.of("sourceUri", expectedSource.toUri().toString()));

    ActionDispatcher actionDispatcher = mock(ActionDispatcher.class);

    FMBPHighlightActionDispatchService service = new FMBPHighlightActionDispatchService();
    service.modelState = modelState;
    service.actionDispatcher = actionDispatcher;
    service.serverSentEventsService = mock(ServerSentEventsService.class);

    ParsedServerSentEvent event =
        new ParsedServerSentEvent(
            "raw",
            Map.of("thread", "AnyThread"),
            Optional.of("requested"),
            Optional.of(Path.of(System.getProperty("java.io.tmpdir"), "other.uvl").toString()),
            Optional.empty(),
            Map.of());

    // Act
    service.dispatchHighlightAction(event);

    // Assert
    verify(actionDispatcher, never()).dispatch(any());
  }

  @Test
  void givenMissingThread_whenDispatchHighlightAction_thenDoesNotDispatch() {
    // Arrange
    BPModelState modelState = mock(BPModelState.class);
    ActionDispatcher actionDispatcher = mock(ActionDispatcher.class);
    FMBPHighlightActionDispatchService service = new FMBPHighlightActionDispatchService();
    service.modelState = modelState;
    service.actionDispatcher = actionDispatcher;
    service.serverSentEventsService = mock(ServerSentEventsService.class);

    ParsedServerSentEvent event =
        new ParsedServerSentEvent(
            "raw",
            Map.of("event", "StartEvent"),
            Optional.of("requested"),
            Optional.empty(),
            Optional.empty(),
            Map.of());

    // Act
    service.dispatchHighlightAction(event);

    // Assert
    verify(actionDispatcher, never()).dispatch(any());
  }

  @Test
  void givenThreadWithoutEventName_whenDispatchHighlightAction_thenResolvesByThreadName() {
    // Arrange
    String threadName = "OnlyThread";
    Feature bThread = new Feature(threadName);
    bThread.getAttributes().put("type", new Attribute<>("type", "BThread", bThread));
    BPFeatureModel featureModel = new BPFeatureModel();
    featureModel.getFeatureMap().put("thread", bThread);

    BPModelState modelState = mock(BPModelState.class);
    UVLModelIndex modelIndex = mock(UVLModelIndex.class);
    when(modelState.getFeatureModel()).thenReturn(featureModel);
    when(modelState.getIndex()).thenReturn(modelIndex);
    when(modelIndex.getIdFor(bThread)).thenReturn(Optional.of("bthread-2"));

    ActionDispatcher actionDispatcher = mock(ActionDispatcher.class);

    FMBPHighlightActionDispatchService service = new FMBPHighlightActionDispatchService();
    service.modelState = modelState;
    service.actionDispatcher = actionDispatcher;
    service.serverSentEventsService = mock(ServerSentEventsService.class);

    ParsedServerSentEvent event =
        new ParsedServerSentEvent(
            "raw",
            Map.of("thread", threadName),
            Optional.of("requested"),
            Optional.empty(),
            Optional.empty(),
            Map.of());

    // Act
    service.dispatchHighlightAction(event);

    // Assert
    ArgumentCaptor<Action> captor = ArgumentCaptor.forClass(Action.class);
    verify(actionDispatcher, times(1)).dispatch(captor.capture());
    HighlightElementAction highlightAction = (HighlightElementAction) captor.getValue();
    assertEquals(List.of("bthread-2"), highlightAction.getElementIds());
  }

  private Feature createBThreadWithEvent(String threadName, String eventName) {
    Feature bThread = new Feature(threadName);
    bThread.getAttributes().put("type", new Attribute<>("type", "BThread", bThread));
    Map<String, Attribute<?>> eventAttrs = new HashMap<>();
    eventAttrs.put("type", new Attribute<>("type", "BEvent", bThread));
    eventAttrs.put("requested", new Attribute<>("requested", true, bThread));
    Attribute<Map<String, Attribute<?>>> event = new Attribute<>(eventName, eventAttrs, bThread);
    bThread.getAttributes().put(eventName, event);
    return bThread;
  }
}
