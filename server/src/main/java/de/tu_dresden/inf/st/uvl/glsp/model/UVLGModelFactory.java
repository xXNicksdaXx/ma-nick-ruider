/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.model;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.utils.GroupUtil;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.*;
import org.eclipse.glsp.graph.util.GConstants;
import org.eclipse.glsp.server.features.core.model.GModelFactory;
import org.eclipse.glsp.server.layout.LayoutEngine;
import org.eclipse.glsp.server.utils.ClientOptionsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil.getAllGroups;

public class UVLGModelFactory implements GModelFactory {

    @Inject
    protected UVLModelState modelState;

    @Inject
    protected LayoutEngine layoutEngine;

    @Override
    public void createGModel() {
        FeatureModel featureModel = modelState.getFeatureModel();
        GGraph newRoot = createRootElement();

        fillRootElement(newRoot, featureModel);
        modelState.updateRoot(newRoot);

        if (requiresLayoutOperation()) {
            layoutEngine.layout(Optional.empty());
        }
    }

    protected GGraph createRootElement() {
        return new GGraphBuilder(DefaultTypes.GRAPH)
                .id(ClientOptionsUtil.getSourceUri(modelState.getClientOptions()).orElse("root"))
                .revision(modelState.getRoot() != null ? modelState.getRoot().getRevision() : -1)
                .build();
    }

    protected void fillRootElement(GGraph root, FeatureModel featureModel) {
        featureModel.getFeatureMap().values().stream()
                .map(this::createFeature)
                .forEachOrdered(root.getChildren()::add);

        getAllGroups(featureModel).stream()
                .map(this::createEdge)
                .flatMap(Collection::stream)
                .forEachOrdered(root.getChildren()::add);
    }

    private GNode createFeature(final Feature feature) {
        UVLModelIndex index = modelState.getIndex();
        String id = index.getIdFor(feature).orElseThrow(
                () -> new IllegalStateException("Feature not indexed: " + feature.getFeatureName())
        );

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
                        .text(feature.getFeatureName())
                        .build());

        Optional<GNode> node = index.getGModelElement(feature, GNode.class);
        if (node.isPresent()) {
            nodeBuilder.position(node.get().getPosition());
            nodeBuilder.size(node.get().getSize());
        } else {
            // initialize with default position and size
            nodeBuilder.position(0, 0);
            nodeBuilder.size(64, 32);
        }
        return nodeBuilder.build();
    }

    private Collection<GEdge> createEdge(final Group group) {
        UVLModelIndex index = modelState.getIndex();
        List<GEdge> edges = new ArrayList<>();

        String groupId = index.getIdFor(group).orElseThrow(
                () -> new IllegalStateException("Group not indexed: " + GroupUtil.getGroupName(group))
        );
        String sourceId = index.getIdFor(group.getParentFeature()).orElseThrow(
                () -> new IllegalStateException("Parent feature of group not indexed: " + group.getParentFeature().getFeatureName())
        );

        String type;
        switch (group.GROUPTYPE) {
            case ALTERNATIVE -> type = UVLModelTypes.ALTERNATIVE;
            case OPTIONAL -> type = UVLModelTypes.OPTIONAL;
            case MANDATORY -> type = UVLModelTypes.MANDATORY;
            case OR -> type = UVLModelTypes.OR;
            default -> throw new IllegalArgumentException("Unknown group type: " + group.GROUPTYPE);
        }

        for (Feature target : group.getFeatures()) {
            String targetId = index.getIdFor(target).orElseThrow(
                    () -> new IllegalStateException("Target feature of group not indexed: " + target.getFeatureName())
            );

            GEdgeBuilder edgeBuilder = new GEdgeBuilder(type)
                    .id(groupId + "_" + targetId)
                    .addCssClass(group.GROUPTYPE.name().toLowerCase())
                    .sourceId(sourceId)
                    .targetId(targetId)
                    .routerKind(GConstants.RouterKind.POLYLINE);

            edges.add(edgeBuilder.build());
        }

        return edges;
    }
    private boolean requiresLayoutOperation() {
        long count = modelState.getRoot().getChildren().stream()
                .filter(element -> element instanceof GNode)
                .map(element -> (GNode) element)
                .filter(node -> node.getPosition().getX() == 0 && node.getPosition().getY() == 0)
                .count();
        return count > 1;
    }
}
