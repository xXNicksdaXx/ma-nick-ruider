/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.gmodel;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.*;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.glsp.graph.*;
import org.eclipse.glsp.graph.builder.impl.*;
import org.eclipse.glsp.graph.util.GConstants;
import org.eclipse.glsp.server.features.core.model.GModelFactory;
import org.eclipse.glsp.server.layout.LayoutEngine;
import org.eclipse.glsp.server.utils.ClientOptionsUtil;

import java.util.Collection;
import java.util.Optional;

import static de.tu_dresden.inf.st.uvl.glsp.layout.UVLTreeLayoutEngine.requiresLayoutOperation;
import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil.*;

public class UVLGModelFactory implements GModelFactory {
    protected static Logger LOGGER = LogManager.getLogger(UVLGModelFactory.class.getSimpleName());

    @Inject
    protected UVLModelState modelState;

    @Inject
    protected LayoutEngine layoutEngine;

    @Inject
    protected UVLFeatureFactory featureFactory;

    @Inject
    protected UVLGroupFactory groupFactory;

    @Inject
    protected UVLBiConstraintFactory biConstraintFactory;

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
                .map(featureFactory::create)
                .forEachOrdered(root.getChildren()::add);

        getAllGroups(featureModel).stream()
                .map(groupFactory::create)
                .flatMap(Collection::stream)
                .forEachOrdered(root.getChildren()::add);

        getEdgeConstraints(featureModel).stream()
                .map(biConstraintFactory::create)
                .forEachOrdered(root.getChildren()::add);

        root.getChildren().add(createConstraintBox(getComplexConstraints(featureModel)));
    }

    protected GNode createConstraintBox(final Collection<Constraint> constraints) {
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
                                .addCssClass("bold-title")
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

    protected GCompartment buildConstraintCompartment(final Collection<Constraint> constraints) {
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

    protected GCompartment createConstraint(final Constraint constraint) {
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
}
