/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp;

import com.google.inject.Singleton;
import de.tu_dresden.inf.st.uvl.glsp.actions.HighlightElementActionHandler;
import de.tu_dresden.inf.st.uvl.glsp.actions.UVLComputedBoundsActionHandler;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.UVLGModelFactory;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLApplyLabelEditOperationHandler;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLCreateAttributeOperationHandler;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLCreateBiConstraintEdgeOperationHandler;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLCreateFeatureCardinalityOperationHandler;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLCreateFeatureOperationHandler;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLCreateGroupCardinalityEdgeOperationHandler;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLCreateRelationEdgeOperationHandler;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLDeleteOperationHandler;
import de.tu_dresden.inf.st.uvl.glsp.layout.UVLTreeLayoutEngine;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelStateImpl;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLSourceModelStorage;
import de.tu_dresden.inf.st.uvl.glsp.palette.UVLToolPaletteItemProvider;
import org.eclipse.glsp.server.actions.ActionHandler;
import org.eclipse.glsp.server.di.DiagramModule;
import org.eclipse.glsp.server.di.MultiBinding;
import org.eclipse.glsp.server.diagram.DiagramConfiguration;
import org.eclipse.glsp.server.features.core.model.ComputedBoundsActionHandler;
import org.eclipse.glsp.server.features.core.model.GModelFactory;
import org.eclipse.glsp.server.features.core.model.SourceModelStorage;
import org.eclipse.glsp.server.features.toolpalette.ToolPaletteItemProvider;
import org.eclipse.glsp.server.gmodel.GModelChangeBoundsOperationHandler;
import org.eclipse.glsp.server.gmodel.GModelChangeRoutingPointsHandler;
import org.eclipse.glsp.server.gmodel.GModelPasteOperationHandler;
import org.eclipse.glsp.server.gmodel.GModelReconnectEdgeOperationHandler;
import org.eclipse.glsp.server.gmodel.GModelRequestClipboardDataActionHandler;
import org.eclipse.glsp.server.layout.LayoutEngine;
import org.eclipse.glsp.server.operations.OperationHandler;

public class UVLDiagramModule extends DiagramModule {

  @Override
  protected void configureBase() {
    super.configureBase();
  }

  @Override
  protected void configure() {
    super.configure();
    configureUVLModelState(bindGModelState());
  }

  @Override
  protected Class<? extends DiagramConfiguration> bindDiagramConfiguration() {
    return UVLDiagramConfiguration.class;
  }

  @Override
  protected Class<? extends UVLModelState> bindGModelState() {
    return UVLModelStateImpl.class;
  }

  @Override
  protected Class<? extends SourceModelStorage> bindSourceModelStorage() {
    return UVLSourceModelStorage.class;
  }

  @Override
  protected Class<? extends GModelFactory> bindGModelFactory() {
    return UVLGModelFactory.class;
  }

  @Override
  protected void configureActionHandlers(final MultiBinding<ActionHandler> binding) {
    super.configureActionHandlers(binding);

    binding.add(GModelRequestClipboardDataActionHandler.class);
    binding.add(HighlightElementActionHandler.class);
    binding.rebind(ComputedBoundsActionHandler.class, UVLComputedBoundsActionHandler.class);
  }

  @Override
  protected void configureOperationHandlers(final MultiBinding<OperationHandler<?>> binding) {
    super.configureOperationHandlers(binding);

    binding.add(GModelChangeBoundsOperationHandler.class);
    binding.add(GModelChangeRoutingPointsHandler.class);
    binding.add(GModelReconnectEdgeOperationHandler.class);
    binding.add(GModelPasteOperationHandler.class);

    binding.add(UVLApplyLabelEditOperationHandler.class);
    binding.add(UVLDeleteOperationHandler.class);

    binding.add(UVLCreateFeatureOperationHandler.class);
    binding.add(UVLCreateAttributeOperationHandler.class);
    binding.add(UVLCreateFeatureCardinalityOperationHandler.class);
    binding.add(UVLCreateRelationEdgeOperationHandler.class);
    binding.add(UVLCreateGroupCardinalityEdgeOperationHandler.class);
    binding.add(UVLCreateBiConstraintEdgeOperationHandler.class);
  }

  @Override
  protected Class<? extends ToolPaletteItemProvider> bindToolPaletteItemProvider() {
    return UVLToolPaletteItemProvider.class;
  }

  @Override
  protected Class<? extends LayoutEngine> bindLayoutEngine() {
    return UVLTreeLayoutEngine.class;
  }

  @Override
  public String getDiagramType() {
    return "uvl-diagram";
  }

  protected void configureUVLModelState(final Class<? extends UVLModelState> uvlStateClass) {
    bind(UVLModelState.class).to(uvlStateClass).in(Singleton.class);
  }
}
