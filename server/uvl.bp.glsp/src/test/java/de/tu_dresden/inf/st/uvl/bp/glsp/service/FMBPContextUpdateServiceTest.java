/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.tu_dresden.inf.st.uvl.bp.glsp.model.BPModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.BPFeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.junit.jupiter.api.Test;

class FMBPContextUpdateServiceTest {

  @Test
  void givenMatchingSourceAndChangedEnvAttribute_whenUpdateContextEnv_thenDispatchesActions() {
    // Arrange
    Feature env = new Feature("Env");
    Attribute<String> modeAttribute = new Attribute<>("mode", "old", env);
    env.getAttributes().put("mode", modeAttribute);
    BPFeatureModel featureModel = new BPFeatureModel();
    featureModel.setEnv(env);

    BPModelState modelState = mock(BPModelState.class);
    Path sourcePath = Path.of(System.getProperty("java.io.tmpdir"), "model.uvl");
    when(modelState.getClientOptions())
        .thenReturn(Map.of("sourceUri", sourcePath.toUri().toString()));
    when(modelState.getFeatureModel()).thenReturn(featureModel);

    ActionDispatcher actionDispatcher = mock(ActionDispatcher.class);
    Action action = mock(Action.class);

    TestableFMBPContextUpdateService service = new TestableFMBPContextUpdateService();
    service.modelState = modelState;
    service.actionDispatcher = actionDispatcher;
    service.submitActions = List.of(action);

    ParsedServerSentEvent event =
        new ParsedServerSentEvent(
            "raw",
            Map.of(),
            Optional.of("context_update"),
            Optional.of(sourcePath.toString()),
            Optional.empty(),
            Map.of("mode", "new"));

    // Act
    service.updateContextEnv(event);

    // Assert
    assertEquals("new", modeAttribute.getValue());
    verify(actionDispatcher, times(1)).dispatchAll(List.of(action));
  }

  @Test
  void givenMismatchedSource_whenUpdateContextEnv_thenDoesNotDispatch() {
    // Arrange
    Feature env = new Feature("Env");
    Attribute<String> modeAttribute = new Attribute<>("mode", "old", env);
    env.getAttributes().put("mode", modeAttribute);
    BPFeatureModel featureModel = new BPFeatureModel();
    featureModel.setEnv(env);

    BPModelState modelState = mock(BPModelState.class);
    when(modelState.getClientOptions())
        .thenReturn(
            Map.of(
                "sourceUri",
                Path.of(System.getProperty("java.io.tmpdir"), "expected.uvl").toUri().toString()));
    when(modelState.getFeatureModel()).thenReturn(featureModel);

    ActionDispatcher actionDispatcher = mock(ActionDispatcher.class);
    TestableFMBPContextUpdateService service = new TestableFMBPContextUpdateService();
    service.modelState = modelState;
    service.actionDispatcher = actionDispatcher;

    ParsedServerSentEvent event =
        new ParsedServerSentEvent(
            "raw",
            Map.of(),
            Optional.of("context_update"),
            Optional.of(Path.of(System.getProperty("java.io.tmpdir"), "other.uvl").toString()),
            Optional.empty(),
            Map.of("mode", "new"));

    // Act
    service.updateContextEnv(event);

    // Assert
    assertEquals("old", modeAttribute.getValue());
    verify(actionDispatcher, never()).dispatchAll(any());
  }

  @Test
  void givenDataWithoutChanges_whenUpdateContextEnv_thenDoesNotDispatch() {
    // Arrange
    Feature env = new Feature("Env");
    Attribute<String> modeAttribute = new Attribute<>("mode", "stable", env);
    env.getAttributes().put("mode", modeAttribute);
    BPFeatureModel featureModel = new BPFeatureModel();
    featureModel.setEnv(env);

    BPModelState modelState = mock(BPModelState.class);
    when(modelState.getFeatureModel()).thenReturn(featureModel);

    ActionDispatcher actionDispatcher = mock(ActionDispatcher.class);
    TestableFMBPContextUpdateService service = new TestableFMBPContextUpdateService();
    service.modelState = modelState;
    service.actionDispatcher = actionDispatcher;

    ParsedServerSentEvent event =
        new ParsedServerSentEvent(
            "raw",
            Map.of(),
            Optional.of("context_update"),
            Optional.empty(),
            Optional.empty(),
            Map.of("mode", "stable"));

    // Act
    service.updateContextEnv(event);

    // Assert
    assertEquals("stable", modeAttribute.getValue());
    verify(actionDispatcher, never()).dispatchAll(any());
  }

  private static class TestableFMBPContextUpdateService extends FMBPContextUpdateService {
    private List<Action> submitActions = List.of();

    @Override
    protected List<Action> submitModel(Feature env) {
      return submitActions;
    }
  }
}
