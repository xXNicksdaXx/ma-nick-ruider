/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.model;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.utils.GroupUtil;
import de.vill.model.Attribute;
import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GCompartment;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.GCompartmentBuilder;
import org.eclipse.glsp.graph.builder.impl.GEdgeBuilder;
import org.eclipse.glsp.graph.builder.impl.GEdgePlacementBuilder;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.builder.impl.GLayoutOptions;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.eclipse.glsp.graph.util.GConstants;
import org.eclipse.glsp.server.features.core.model.GModelFactory;
import org.eclipse.glsp.server.layout.LayoutEngine;
import org.eclipse.glsp.server.utils.ClientOptionsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static de.tu_dresden.inf.st.uvl.glsp.layout.UVLTreeLayoutEngine.requiresLayoutOperation;
import static de.tu_dresden.inf.st.uvl.glsp.utils.ConstraintUtil.convertConstraintTypeToModelType;
import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil.getAllGroups;
import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil.getComplexConstraints;
import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil.getEdgeConstraints;

public class UVLGModelFactory implements GModelFactory {
    protected static Logger LOGGER = LogManager.getLogger(UVLGModelFactory.class.getSimpleName());

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

        if (requiresLayoutOperation(modelState.getRoot())) {
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
                .map(this::createGroupEdges)
                .flatMap(Collection::stream)
                .forEachOrdered(root.getChildren()::add);

        getEdgeConstraints(featureModel).stream()
                .map(this::createBiConstraintEdge)
                .forEachOrdered(root.getChildren()::add);

        root.getChildren().add(createConstraintBox(getComplexConstraints(featureModel)));
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
                        .paddingTop(0)
                        .paddingLeft(0)
                        .paddingRight(0)
                        .paddingBottom(0.0)
                        .resizeContainer(true))
                .add(buildHeader(id, feature.getFeatureName(), feature.getLowerBound(), feature.getUpperBound()))
                .add(buildAttributeCompartment(id, feature));

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

    private GCompartment buildHeader(final String id, final String name, String lower, String upper) {
        GLabel headerLabel = new GLabelBuilder(UVLModelTypes.FEATURE_NAME)
                .id(id + "_header_label")
                .text(name)
                .build();
        GCompartmentBuilder headerBuilder = new GCompartmentBuilder(DefaultTypes.COMPARTMENT_HEADER)
                .id(id + "_header")
                .layout(GConstants.Layout.HBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(4)
                        .paddingLeft(4)
                        .paddingRight(4)
                        .paddingBottom(4.0)
                        .hAlign(GConstants.HAlign.CENTER)
                        .resizeContainer(true))
                .add(headerLabel);

        if (lower != null && upper != null) {
            GLabel cardinalityLabel = new GLabelBuilder(UVLModelTypes.CARDINALITY_LABEL)
                    .id(id + "_cardinality_label")
                    .text(lower + ".." + upper)
                    .build();
            headerBuilder
                    .add(new GLabelBuilder(DefaultTypes.LABEL)
                            .text(" [")
                            .build())
                    .add(cardinalityLabel)
                    .add(new GLabelBuilder(DefaultTypes.LABEL)
                            .text("]")
                            .build());
        }

        return headerBuilder.build();
    }

    private GCompartment buildAttributeCompartment(final String id, final Feature feature) {
        GCompartmentBuilder compartmentBuilder = new GCompartmentBuilder(DefaultTypes.COMPARTMENT)
                .id(id + "_attribute_compartment")
                .layout(GConstants.Layout.VBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(0)
                        .paddingLeft(0)
                        .paddingRight(0)
                        .paddingBottom(0.0)
                        .hAlign(GConstants.HAlign.LEFT)
                        .resizeContainer(true));

        int i = 0;
        for (Attribute<?> attribute : feature.getAttributes().values()) {
            compartmentBuilder.add(createAttribute(id, i, attribute));
            i++;
        }

        return compartmentBuilder.build();
    }

    private GCompartment createAttribute(final String id, final int index, final Attribute<?> attribute) {
        String attributeId = id + "_attribute_" + index;

        return new GCompartmentBuilder(UVLModelTypes.ATTRIBUTE)
                .id(attributeId)
                .addCssClass("attribute")
                .layout(GConstants.Layout.HBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(2)
                        .paddingLeft(2)
                        .paddingRight(2)
                        .paddingBottom(2.0)
                        .resizeContainer(true))
                .add(new GLabelBuilder(UVLModelTypes.ATTRIBUTE_NAME)
                        .id(attributeId + "_name")
                        .text(attribute.getName())
                        .build())
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .text(" = ")
                        .build())
                .add(new GLabelBuilder(UVLModelTypes.ATTRIBUTE_VALUE)
                        .id(attributeId + "_value")
                        .text(attribute.getValue().toString())
                        .build())
                .build();
    }

    private Collection<GEdge> createGroupEdges(final Group group) {
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
            case MANDATORY -> type = UVLModelTypes.MANDATORY;
            case OPTIONAL -> type = UVLModelTypes.OPTIONAL;
            case ALTERNATIVE -> type = UVLModelTypes.ALTERNATIVE;
            case GROUP_CARDINALITY -> type = UVLModelTypes.GROUP_CARDINALITY;
            case OR -> type = UVLModelTypes.OR;
            default -> throw new IllegalArgumentException("Unknown group type: " + group.GROUPTYPE);
        }

        boolean requiresCardinalityLabel = group.GROUPTYPE == Group.GroupType.GROUP_CARDINALITY;

        for (Feature target : group.getFeatures()) {
            String targetId = index.getIdFor(target).orElseThrow(
                    () -> new IllegalStateException("Target feature of group not indexed: " + target.getFeatureName())
            );

            GEdgeBuilder edgeBuilder = new GEdgeBuilder(type)
                    .id(groupId + "_" + targetId)
                    .addCssClass("centered-anchor")
                    .addCssClass(group.GROUPTYPE.name().toLowerCase())
                    .sourceId(sourceId)
                    .targetId(targetId)
                    .routerKind(GConstants.RouterKind.POLYLINE);

            if (requiresCardinalityLabel) {
                edgeBuilder.add(new GLabelBuilder(UVLModelTypes.CARDINALITY_LABEL)
                        .id(groupId + "_" + targetId + "_label")
                        .text(GroupUtil.getCardinalityText(group))
                        .addCssClass("edge-label")
                        .edgePlacement(new GEdgePlacementBuilder()
                                .side(GConstants.EdgeSide.LEFT)
                                .position(0.1d)
                                .rotate(false)
                                .build())
                        .build());
                requiresCardinalityLabel = false;
            }

            edges.add(edgeBuilder.build());
        }

        return edges;
    }

    private GEdge createBiConstraintEdge(Constraint constraint) {
        UVLModelIndex index = modelState.getIndex();

        String id = index.getIdFor(constraint).orElseThrow(
                () -> new IllegalStateException("Constraint not indexed: " + constraint.toString())
        );

        String sourceId, targetId;

        Constraint sourceConstraint = constraint.getConstraintSubParts().getFirst();
        if (sourceConstraint instanceof LiteralConstraint sourceLiteralConstraint) {
            Feature sourceFeature = sourceLiteralConstraint.getFeature();
            sourceId = index.getIdFor(sourceFeature).orElseThrow(
                    () -> new IllegalStateException("Source feature of implication constraint not indexed: " + sourceFeature.getFeatureName())
            );
        } else {
            throw new IllegalStateException("Unsupported source constraint type for implication: " + sourceConstraint.getClass().getName());
        }

        Constraint targetConstraint = constraint.getConstraintSubParts().getLast();
        if (targetConstraint instanceof LiteralConstraint targetLiteralConstraint) {
            Feature targetFeature = targetLiteralConstraint.getFeature();
            targetId = index.getIdFor(targetFeature).orElseThrow(
                    () -> new IllegalStateException("Target feature of implication constraint not indexed: " + targetFeature.getFeatureName())
            );
        } else {
            throw new IllegalStateException("Unsupported target constraint type for implication: " + targetConstraint.getClass().getName());
        }

        String type = convertConstraintTypeToModelType(constraint);

        GEdgeBuilder edgeBuilder = new GEdgeBuilder(type)
                .id(id)
                .sourceId(sourceId)
                .targetId(targetId)
                .routerKind(GConstants.RouterKind.POLYLINE)
                .addCssClass("constraint-edge")
                .add(new GLabelBuilder(UVLModelTypes.CONSTRAINT_LABEL)
                        .id(id + "_label")
                        .text("requires")
                        .addCssClass("edge-label")
                        .edgePlacement(new GEdgePlacementBuilder()
                                .side(GConstants.EdgeSide.TOP)
                                .position(0.5d)
                                .offset(1.5d)
                                .rotate(false)
                                .build())
                        .build());

        applyEdgeData(edgeBuilder, id);
        return edgeBuilder.build();
    }

    private GNode createConstraintBox(final Collection<Constraint> constraints) {
        UVLModelIndex index = modelState.getIndex();

        GNodeBuilder boxBuilder = new GNodeBuilder(UVLModelTypes.CONSTRAINT_BOX)
                .id("constraint_box")
                .layout(GConstants.Layout.VBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(0)
                        .paddingLeft(0)
                        .paddingRight(0)
                        .paddingBottom(0.0)
                        .resizeContainer(true))
                .add(new GCompartmentBuilder(DefaultTypes.COMPARTMENT_HEADER)
                        .id("constraint_box_header")
                        .layout(GConstants.Layout.HBOX)
                        .layoutOptions(new GLayoutOptions()
                                .paddingTop(4)
                                .paddingLeft(4)
                                .paddingRight(4)
                                .paddingBottom(4.0)
                                .hAlign(GConstants.HAlign.CENTER)
                                .resizeContainer(true))
                        .add(new GLabelBuilder(DefaultTypes.LABEL)
                                .id("constraint_box_header_label")
                                .text("Constraints")
                                .build())
                        .build())
                .add(buildConstraintCompartment(constraints));

        Optional<GNode> node = index.getGModelElement("constraint_box", GNode.class);
        if (node.isPresent()) {
            boxBuilder.position(node.get().getPosition());
            boxBuilder.size(node.get().getSize());
        } else {
            // initialize with default position and size
            boxBuilder.position(-64, -64);
            boxBuilder.size(64, 32);
        }

        return boxBuilder.build();
    }

    private GCompartment buildConstraintCompartment(final Collection<Constraint> constraints) {
        GCompartmentBuilder compartmentBuilder = new GCompartmentBuilder(DefaultTypes.COMPARTMENT)
                .id("constraint_box_compartment")
                .layout(GConstants.Layout.VBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(0)
                        .paddingLeft(0)
                        .paddingRight(0)
                        .paddingBottom(0.0)
                        .hAlign(GConstants.HAlign.LEFT)
                        .resizeContainer(true));

        constraints.stream()
                .map(this::createConstraint)
                .forEachOrdered(compartmentBuilder::add);

        return compartmentBuilder.build();
    }

    private GCompartment createConstraint(final Constraint constraint) {
        UVLModelIndex index = modelState.getIndex();
        String constraintId = index.getIdFor(constraint).orElseThrow(
                () -> new IllegalStateException("Constraint not indexed: " + constraint.toString())
        );

        return new GCompartmentBuilder(UVLModelTypes.CONSTRAINT)
                .id(constraintId)
                .layout(GConstants.Layout.HBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(2)
                        .paddingLeft(2)
                        .paddingRight(2)
                        .paddingBottom(2.0)
                        .resizeContainer(true))
                .add(new GLabelBuilder(UVLModelTypes.CONSTRAINT_TEXT)
                        .id(constraintId + "_text")
                        .text(constraint.toString(false, ""))
                        .build())
                .build();
    }

    protected void applyEdgeData(GEdgeBuilder edgeBuilder, String id) {
        Optional<GEdge> existingEdge = modelState.getIndex().getGModelElement(id, GEdge.class);
        existingEdge.ifPresent(edge -> edgeBuilder
                .addRoutingPoints(edge.getRoutingPoints()));
    }
}
