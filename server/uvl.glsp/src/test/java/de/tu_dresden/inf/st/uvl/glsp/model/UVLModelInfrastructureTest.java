/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.tu_dresden.inf.st.uvl.glsp.UVLDiagramModule;
import de.tu_dresden.inf.st.uvl.glsp.actions.UVLComputedBoundsActionHandler;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;
import org.eclipse.glsp.server.actions.ActionHandler;
import org.eclipse.glsp.server.features.core.model.ComputedBoundsActionHandler;
import org.eclipse.glsp.server.gmodel.GModelChangeBoundsOperationHandler;
import org.eclipse.glsp.server.gmodel.GModelRequestClipboardDataActionHandler;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.OperationHandler;
import org.eclipse.glsp.server.session.ClientSession;
import org.eclipse.glsp.server.types.GLSPServerException;
import org.junit.jupiter.api.Test;

class UVLModelInfrastructureTest {

  @Test
  void givenFeatureModel_whenUpdateRoot_thenIndexesFeatureModel() {
    // Arrange
    GGraph root = new GGraphBuilder(DefaultTypes.GRAPH).id("root").build();
    TrackingModelIndex index = new TrackingModelIndex(root);
    TestableModelState state = new TestableModelState(index);
    FeatureModel featureModel = new FeatureModel();
    state.setFeatureModel(featureModel);

    // Act
    state.updateRoot(root);

    // Assert
    assertSame(index, state.getIndex());
    assertTrue(index.wasIndexed());
    assertSame(featureModel, index.getIndexedModel());
  }

  @Test
  void givenModelState_whenSessionDisposed_thenClearsIndex() throws Exception {
    // Arrange
    UVLModelStateImpl state = new UVLModelStateImpl();
    FeatureModel featureModel = new FeatureModel();
    state.setFeatureModel(featureModel);
    assertSame(featureModel, state.getFeatureModel());

    UVLModelIndex index = mock(UVLModelIndex.class);
    injectIndex(state, index);
    assertSame(index, state.getIndex());

    // Act
    ClientSession session = mock(ClientSession.class);
    state.sessionDisposed(session);

    // Assert
    verify(index).clear();
  }

  @Test
  void givenWhitespaceFile_whenParsing_thenReturnsEmptyFeatureModel() throws Exception {
    // Arrange
    ExposedSourceModelStorage storage = new ExposedSourceModelStorage();
    GModelState modelState = mock(GModelState.class);
    when(modelState.getClientOptions())
        .thenReturn(Map.of("sourceUri", "file:///C:/temp/model.uvl"));
    when(modelState.getRoot()).thenReturn(null);

    // Act
    GGraph root = storage.exposeCreateNewEmptyRoot(modelState);
    Path tempFile = Files.createTempFile("uvl-glsp", ".uvl");
    Files.writeString(tempFile, "   ");
    FeatureModel parsed = storage.exposeParseFeatureModel(tempFile);

    // Assert
    assertEquals("file:///C:/temp/model.uvl", root.getId());
    assertEquals(-1, root.getRevision());
    assertEquals(DefaultTypes.GRAPH, root.getType());
    assertTrue(parsed.getFeatureMap().isEmpty());
    Files.deleteIfExists(tempFile);
  }

  @Test
  void givenMissingFeatureModelFile_whenParsing_thenThrowsGlspServerException() {
    // Arrange
    ExposedSourceModelStorage storage = new ExposedSourceModelStorage();
    Path missingFile = Path.of(System.getProperty("java.io.tmpdir")).resolve("missing.uvl");

    // Act + Assert
    assertThrows(GLSPServerException.class, () -> storage.exposeParseFeatureModel(missingFile));
  }

  @Test
  void givenMissingSourceUri_whenCreatingRoot_thenDefaultsToRootId() {
    // Arrange
    ExposedSourceModelStorage storage = new ExposedSourceModelStorage();
    GModelState modelState = mock(GModelState.class);
    when(modelState.getClientOptions()).thenReturn(Map.of());
    when(modelState.getRoot()).thenReturn(null);

    // Act
    GGraph root = storage.exposeCreateNewEmptyRoot(modelState);

    // Assert
    assertEquals("root", root.getId());
    assertEquals(-1, root.getRevision());
  }

  @Test
  void givenDiagramModule_whenConfiguringHandlers_thenRegistersRequiredHandlers() {
    // Arrange
    ExposedDiagramModule module = new ExposedDiagramModule();
    @SuppressWarnings("unchecked")
    org.eclipse.glsp.server.di.MultiBinding<ActionHandler> actionBinding =
        mock(org.eclipse.glsp.server.di.MultiBinding.class);
    @SuppressWarnings("unchecked")
    org.eclipse.glsp.server.di.MultiBinding<OperationHandler<?>> operationBinding =
        mock(org.eclipse.glsp.server.di.MultiBinding.class);

    // Act
    module.exposeConfigureActionHandlers(actionBinding);
    module.exposeConfigureOperationHandlers(operationBinding);

    // Assert
    verify(actionBinding).add(GModelRequestClipboardDataActionHandler.class);
    verify(actionBinding)
        .rebind(ComputedBoundsActionHandler.class, UVLComputedBoundsActionHandler.class);
    verify(operationBinding).add(GModelChangeBoundsOperationHandler.class);
  }

  private static void injectIndex(Object target, Object value) throws Exception {
    Field field = findField(target.getClass());
    field.setAccessible(true);
    field.set(target, value);
  }

  private static Field findField(Class<?> type) throws Exception {
    Class<?> current = type;
    while (current != null) {
      try {
        return current.getDeclaredField("index");
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException("index");
  }

  private static final class ExposedSourceModelStorage extends UVLSourceModelStorage {
    GGraph exposeCreateNewEmptyRoot(GModelState modelState) {
      return (GGraph) createNewEmptyRoot(modelState);
    }

    FeatureModel exposeParseFeatureModel(Path path) {
      return parseFeatureModel(path);
    }
  }

  private static final class ExposedDiagramModule extends UVLDiagramModule {
    void exposeConfigureActionHandlers(
        org.eclipse.glsp.server.di.MultiBinding<ActionHandler> binding) {
      configureActionHandlers(binding);
    }

    void exposeConfigureOperationHandlers(
        org.eclipse.glsp.server.di.MultiBinding<OperationHandler<?>> binding) {
      configureOperationHandlers(binding);
    }
  }

  private static final class TrackingModelIndex extends UVLModelIndex {
    private boolean indexed;
    private FeatureModel indexedModel;

    TrackingModelIndex(EObject target) {
      super(target);
    }

    @Override
    protected void indexFeatureModel(final FeatureModel featureModel) {
      this.indexed = true;
      this.indexedModel = featureModel;
    }

    boolean wasIndexed() {
      return indexed;
    }

    FeatureModel getIndexedModel() {
      return indexedModel;
    }
  }

  private static final class TestableModelState extends UVLModelStateImpl {
    private final UVLModelIndex index;

    private TestableModelState(UVLModelIndex index) {
      this.index = index;
    }

    @Override
    protected UVLModelIndex getOrUpdateIndex(final org.eclipse.glsp.graph.GModelRoot newRoot) {
      return index;
    }
  }
}
