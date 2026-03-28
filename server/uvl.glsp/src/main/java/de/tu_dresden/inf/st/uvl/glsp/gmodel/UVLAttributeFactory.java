/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.gmodel;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.generic.AbstractSingleGModelFactory;
import de.tu_dresden.inf.st.uvl.glsp.utils.GModelUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GCompartment;
import org.eclipse.glsp.graph.builder.impl.GCompartmentBuilder;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.builder.impl.GLayoutOptions;
import org.eclipse.glsp.graph.util.GConstants;

import java.util.Map;

public class UVLAttributeFactory extends AbstractSingleGModelFactory<Attribute<?>, GCompartment> {

    @Override
    public GCompartment create(Attribute<?> attribute) {
        Feature parentFeature = attribute.getFeature();
        if (parentFeature == null) {
            throw new IllegalArgumentException("Attribute does not have a parent feature: " + attribute.getName());
        }

        String parentFeatureId = modelState.getIndex().getIdFor(parentFeature)
                .orElseThrow(() -> new IllegalStateException("Parent feature not indexed for attribute: " + attribute.getName()));

        return createAttribute(parentFeatureId, attribute.getName(), attribute);
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

