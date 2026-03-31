/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp;

import com.google.inject.Singleton;
import de.tu_dresden.inf.st.uvl.bp.glsp.gmodel.BPGModelFactory;
import de.tu_dresden.inf.st.uvl.bp.glsp.handler.BPApplyLabelEditOperationHandler;
import de.tu_dresden.inf.st.uvl.bp.glsp.handler.BPCreateBThreadOperationHandler;
import de.tu_dresden.inf.st.uvl.bp.glsp.handler.BPCreateEventOperationHandler;
import de.tu_dresden.inf.st.uvl.bp.glsp.handler.BPDeleteOperationHandler;
import de.tu_dresden.inf.st.uvl.bp.glsp.model.BPModelState;
import de.tu_dresden.inf.st.uvl.bp.glsp.model.BPModelStateImpl;
import de.tu_dresden.inf.st.uvl.bp.glsp.model.BPSourceModelStorage;
import de.tu_dresden.inf.st.uvl.bp.glsp.palette.BPToolPaletteItemProvider;
import de.tu_dresden.inf.st.uvl.glsp.UVLDiagramModule;
import de.tu_dresden.inf.st.uvl.glsp.handler.*;
import org.eclipse.glsp.server.di.MultiBinding;
import org.eclipse.glsp.server.diagram.DiagramConfiguration;
import org.eclipse.glsp.server.features.core.model.GModelFactory;
import org.eclipse.glsp.server.features.core.model.SourceModelStorage;
import org.eclipse.glsp.server.features.toolpalette.ToolPaletteItemProvider;
import org.eclipse.glsp.server.operations.OperationHandler;

public class BPDiagramModule extends UVLDiagramModule {

  @Override
  protected void configure() {
    super.configure();
    configureBPModelState(bindGModelState());
  }

  @Override
  protected Class<? extends DiagramConfiguration> bindDiagramConfiguration() {
    return BPDiagramConfiguration.class;
  }

  @Override
  protected Class<? extends BPModelState> bindGModelState() {
    return BPModelStateImpl.class;
  }

  @Override
  protected Class<? extends SourceModelStorage> bindSourceModelStorage() {
    return BPSourceModelStorage.class;
  }

  @Override
  protected Class<? extends GModelFactory> bindGModelFactory() {
    return BPGModelFactory.class;
  }

  @Override
  protected void configureOperationHandlers(final MultiBinding<OperationHandler<?>> binding) {
    super.configureOperationHandlers(binding);

    binding.rebind(UVLApplyLabelEditOperationHandler.class, BPApplyLabelEditOperationHandler.class);
    binding.rebind(UVLDeleteOperationHandler.class, BPDeleteOperationHandler.class);
    binding.add(BPCreateBThreadOperationHandler.class);
    binding.add(BPCreateEventOperationHandler.class);
  }

  @Override
  protected Class<? extends ToolPaletteItemProvider> bindToolPaletteItemProvider() {
    return BPToolPaletteItemProvider.class;
  }

  protected void configureBPModelState(final Class<? extends BPModelState> bpStateClass) {
    bind(BPModelState.class).to(bpStateClass).in(Singleton.class);
  }
}
