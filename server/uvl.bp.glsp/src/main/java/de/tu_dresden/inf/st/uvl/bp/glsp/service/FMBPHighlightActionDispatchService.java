/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tu_dresden.inf.st.uvl.bp.glsp.model.BPModelState;
import de.tu_dresden.inf.st.uvl.bp.glsp.utils.BTypeUtil;
import de.tu_dresden.inf.st.uvl.glsp.actions.HighlightElementAction;
import java.nio.file.Path;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.glsp.server.actions.ActionDispatcher;

/** Parses SSE payloads from the FMBP event listener and dispatches matching highlight actions. */
@Singleton
public class FMBPHighlightActionDispatchService {

  private static final Logger LOGGER =
      LogManager.getLogger(FMBPHighlightActionDispatchService.class.getName());

  @Inject protected ServerSentEventsService serverSentEventsService;

  @Inject protected ActionDispatcher actionDispatcher;

  @Inject protected BPModelState modelState;

  @Inject
  protected void registerDataListener() {
    serverSentEventsService.addDataListener(
        this::dispatchHighlightAction, "requested", "blocked", "waited_for");
  }

  protected void dispatchHighlightAction(final ParsedServerSentEvent event) {
    Set<String> matchingFeatureIds;
    Optional<Path> sourcePath = parseSourcePath(event);
    if (sourcePath.isPresent()) {
      Map<String, String> options = modelState.getClientOptions();
      if (options == null) {
        return;
      }

      // check if source URI from FMBP matches the one of GLSP
      String sourceUri = options.get("sourceUri");
      Path expectedSourcePath = Path.of(java.net.URI.create(sourceUri));
      if (!sourcePath.get().equals(expectedSourcePath)) {
        return;
      }
    }

    Optional<String> threadName = parseThreadName(event);
    if (threadName.isEmpty()) {
      return;
    }

    Optional<String> eventName = parseEventName(event);

    if (eventName.isPresent()) {
      matchingFeatureIds = resolveFeatureIdsByEventName(threadName.get(), eventName.get());
    } else {
      matchingFeatureIds = resolveFeatureIdsByThreadName(threadName.get());
    }

    actionDispatcher.dispatch(new HighlightElementAction(List.copyOf(matchingFeatureIds), true));
  }

  protected Optional<String> parseThreadName(final ParsedServerSentEvent event) {
    return parsePayloadElement(event.payload(), "thread");
  }

  protected Optional<String> parseEventName(final ParsedServerSentEvent event) {
    return parsePayloadElement(event.payload(), "event");
  }

  protected Optional<Path> parseSourcePath(final ParsedServerSentEvent event) {
    return event.source().map(Path::of);
  }

  protected Optional<String> parsePayloadElement(final Map<?, ?> payload, final String key) {
    if (payload.containsKey(key)) {
      Object value = payload.get(key);
      if (value instanceof String stringValue && !stringValue.isBlank()) {
        return Optional.of(stringValue.trim());
      }
    }
    return Optional.empty();
  }

  protected Set<String> resolveFeatureIdsByThreadName(final String threadName) {
    if (modelState.getFeatureModel() == null || threadName == null) {
      return Set.of();
    }

    Set<String> ids = new LinkedHashSet<>();
    BTypeUtil.getAllBThreads(modelState.getFeatureModel()).stream()
        .filter(feature -> feature.getFeatureName().equals(threadName))
        .map(feature -> modelState.getIndex().getIdFor(feature))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(ids::add);

    return ids;
  }

  protected Set<String> resolveFeatureIdsByEventName(
      final String threadName, final String eventName) {
    if (modelState.getFeatureModel() == null || threadName == null || eventName == null) {
      return Set.of();
    }

    Set<String> ids = new LinkedHashSet<>();
    BTypeUtil.getAllBThreadsWithBEvent(modelState.getFeatureModel(), eventName).stream()
        .filter(feature -> feature.getFeatureName().equals(threadName))
        .map(feature -> modelState.getIndex().getIdFor(feature))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(ids::add);

    return ids;
  }
}
