/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.graph.*;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.builder.impl.GLayoutOptions;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.eclipse.glsp.graph.util.GConstants;
import org.eclipse.glsp.graph.util.GraphUtil;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.actions.SelectAction;
import org.eclipse.glsp.server.gmodel.GModelCreateOperationHandler;
import org.eclipse.glsp.server.operations.CreateNodeOperation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelEditUtil.addFeature;
import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelInfoUtil.hasRootFeature;

public class UVLCreateFeatureOperationHandler extends GModelCreateOperationHandler<CreateNodeOperation> {

    protected static Logger LOGGER = LogManager.getLogger(UVLCreateFeatureOperationHandler.class.getSimpleName());

    @Inject
    protected UVLModelState modelState;

    @Inject
    protected ActionDispatcher actionDispatcher;

    UVLCreateFeatureOperationHandler() {
        super(UVLModelTypes.FEATURE);
    }

    @Override
    public Optional<Command> createCommand(CreateNodeOperation operation) {
        return commandOf(() -> executeCreation(operation));
    }

    protected void executeCreation(CreateNodeOperation operation) {
        if (!hasRootFeature(modelState.getFeatureModel())) {
            GNode rootNode = createGNode(operation.getLocation());
            createFeature(Optional.empty());
            selectElement(rootNode);
        } else {
            Feature parentFeature = getInitialFeatureLink(operation.getContainerId());
            GNode parentNode = modelState.getIndex().getGModelElement(parentFeature, GNode.class).orElseThrow(() ->
                    new IllegalStateException("No GNode found for parent feature: " + parentFeature.getFeatureName()));
            GPoint location = calculateNewFeatureLocation(parentNode.getPosition());
            GNode newNode = createGNode(Optional.of(location));
            createFeature(Optional.of(parentFeature));
            selectElement(newNode);
        }
        modelState.updateRoot(modelState.getRoot());
    }

    protected Feature getInitialFeatureLink(String containerId) {
        Optional<Feature> parentFeature = modelState.getIndex().getUVLObject(containerId, Feature.class);
        if (parentFeature.isPresent()) {
            return parentFeature.get();
        } else {
            LOGGER.trace("No parent feature found for container ID: {}. Using root feature as parent.", containerId);
            return modelState.getFeatureModel().getRootFeature();
        }
    }

    protected GNode createGNode(Optional<GPoint> location) {
        String id = UUID.randomUUID().toString();

        GNodeBuilder nodeBuilder = new GNodeBuilder(UVLModelTypes.FEATURE)
                .id(id)
                .layout(GConstants.Layout.VBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(2)
                        .paddingLeft(2)
                        .paddingRight(2)
                        .paddingBottom(2.0))
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .id(id + "_label")
                        .text(getFeatureName())
                        .build())
                .size(64, 32);

        if (location.isPresent()) {
            nodeBuilder.position(location.get().getX(), location.get().getY());
        } else {
            nodeBuilder.position(0, 0);
        }

        GNode node = nodeBuilder.build();
        modelState.getRoot().getChildren().add(node);
        return node;
    }

    protected void createFeature(Optional<Feature> parentFeatureOpt) {
        Feature feature = new Feature(getFeatureName());
        FeatureModel featureModel = addFeature(modelState.getFeatureModel(), feature, parentFeatureOpt);
        modelState.setFeatureModel(featureModel);
    }

    protected void selectElement(GNode node) {
        actionDispatcher.dispatchAfterNextUpdate(SelectAction.addSelection(List.of(node.getId())));
    }

    private String getFeatureName() {
        int currentFeatureCount = modelState.getFeatureModel().getFeatureMap().size();
        return "Feature" + (currentFeatureCount + 1);
    }

    private GPoint calculateNewFeatureLocation(GPoint parentPosition) {
        final double level_spacing = 80.0;
        double x = parentPosition.getX() + (Math.random() * 256 - 128);
        double y = parentPosition.getY() + level_spacing;
        return GraphUtil.point(x, y);
    }
}
