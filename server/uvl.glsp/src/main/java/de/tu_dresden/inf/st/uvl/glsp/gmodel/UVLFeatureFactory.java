/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.gmodel;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.generic.AbstractSingleGModelFactory;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil;
import de.tu_dresden.inf.st.uvl.glsp.utils.GModelUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Cardinality;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GCompartment;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.GCompartmentBuilder;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.builder.impl.GLayoutOptions;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.eclipse.glsp.graph.util.GConstants;

import java.util.Map;

public class UVLFeatureFactory extends AbstractSingleGModelFactory<Feature, GNode> {

    @Override
    protected GNode create(final Feature feature) {
        return createFeature(feature);
    }

    protected GNode createFeature(final Feature feature) {
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
                .add(buildHeader(id, feature.getFeatureName(), feature.getCardinality()))
                .add(buildAttributeCompartment(id, feature));

        applyNodeData(nodeBuilder, id);
        return nodeBuilder.build();
    }

    protected GCompartment buildHeader(final String id, final String name, final Cardinality cardinality) {
        GLabel headerLabel = new GLabelBuilder(UVLModelTypes.FEATURE_NAME)
                .id(id + "_header_label")
                .addCssClass("bold-title")
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

        if (cardinality != null) {
            addCardinality(cardinality, id,  headerBuilder);
        }

        return headerBuilder.build();
    }

    protected void addCardinality(final Cardinality cardinality, final String id, GCompartmentBuilder compartmentBuilder) {
        GLabel cardinalityLabel = new GLabelBuilder(UVLModelTypes.CARDINALITY_LABEL)
                .id(id + "_cardinality_label")
                .text(FeatureModelUtil.getCardinalityText(cardinality))
                .build();
        compartmentBuilder
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .text(" [")
                        .build())
                .add(cardinalityLabel)
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .text("]")
                        .build());
    }

    protected GCompartment buildAttributeCompartment(final String id, final Feature feature) {
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

        for (Map.Entry<String, Attribute<?>> entry : feature.getAttributes().entrySet()) {
            compartmentBuilder.add(createAttribute(id, entry.getKey(), entry.getValue()));
        }

        return compartmentBuilder.build();
    }

    protected GCompartment createAttribute(final String id, final String attributeName, final Attribute<?> attribute) {
        return createAttribute(GModelUtil.appendAttributeSegment(id, attributeName), attribute, 0);
    }

    protected GCompartment createAttribute(final String attributeId, final Attribute<?> attribute, final int indentLevel) {
        Map<String, Attribute<?>> subAttributes = getSubAttributes(attribute);

        if (subAttributes == null) {
            return createLeafAttribute(attributeId, attribute, indentLevel);
        }

        GCompartmentBuilder attributeCompartmentBuilder = new GCompartmentBuilder(UVLModelTypes.ATTRIBUTE)
                .id(attributeId)
                .addCssClass("attribute")
                .layout(GConstants.Layout.VBOX)
                .layoutOptions(new GLayoutOptions()
                        .hAlign(GConstants.HAlign.LEFT)
                        .resizeContainer(true))
                .add(new GCompartmentBuilder(DefaultTypes.COMPARTMENT)
                        .id(attributeId + "_open")
                        .layout(GConstants.Layout.HBOX)
                        .layoutOptions(new GLayoutOptions()
                                .hAlign(GConstants.HAlign.LEFT)
                                .resizeContainer(true))
                        .add(new GLabelBuilder(DefaultTypes.LABEL)
                                .text(indent(indentLevel))
                                .build())
                        .add(new GLabelBuilder(UVLModelTypes.ATTRIBUTE_NAME)
                                .id(attributeId + "_name")
                                .text(attribute.getName())
                                .build())
                        .add(new GLabelBuilder(DefaultTypes.LABEL)
                                .text(" = {")
                                .build())
                        .build());

        GCompartmentBuilder childrenCompartmentBuilder = new GCompartmentBuilder(DefaultTypes.COMPARTMENT)
                .id(attributeId + "_children")
                .layout(GConstants.Layout.VBOX)
                .layoutOptions(new GLayoutOptions()
                        .hAlign(GConstants.HAlign.LEFT)
                        .resizeContainer(true));

        for (Map.Entry<String, Attribute<?>> entry : subAttributes.entrySet()) {
            childrenCompartmentBuilder.add(createAttribute(
                    GModelUtil.appendAttributeSegment(attributeId, entry.getKey()),
                    entry.getValue(),
                    indentLevel + 1
            ));
        }

        return attributeCompartmentBuilder
                .add(childrenCompartmentBuilder.build())
                .add(new GCompartmentBuilder(DefaultTypes.COMPARTMENT)
                        .id(attributeId + "_close")
                        .layout(GConstants.Layout.HBOX)
                        .layoutOptions(new GLayoutOptions()
                                .hAlign(GConstants.HAlign.LEFT)
                                .resizeContainer(true))
                        .add(new GLabelBuilder(DefaultTypes.LABEL)
                                .text(indent(indentLevel))
                                .build())
                        .add(new GLabelBuilder(DefaultTypes.LABEL)
                                .text("}")
                                .build())
                        .build())
                .build();
    }

    protected GCompartment createLeafAttribute(final String attributeId, final Attribute<?> attribute, final int indentLevel) {
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
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .text(indent(indentLevel))
                        .build())
                .add(new GLabelBuilder(UVLModelTypes.ATTRIBUTE_NAME)
                        .id(attributeId + "_name")
                        .text(attribute.getName())
                        .build())
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .text(" = ")
                        .build())
                .add(new GLabelBuilder(UVLModelTypes.ATTRIBUTE_VALUE)
                        .id(attributeId + "_value")
                        .text(formatAttributeValue(attribute))
                        .build())
                .build();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Attribute<?>> getSubAttributes(final Attribute<?> attribute) {
        Object value = attribute.getValue();
        if (!(value instanceof Map<?, ?> rawMap)) {
            return null;
        }

        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof Attribute)) {
                return null;
            }
        }
        return (Map<String, Attribute<?>>) rawMap;
    }

    protected String indent(final int indentLevel) {
        return "  ".repeat(Math.max(0, indentLevel));
    }

    protected String formatAttributeValue(final Attribute<?> attribute) {
        return attribute.getValue().toString();
    }
}
