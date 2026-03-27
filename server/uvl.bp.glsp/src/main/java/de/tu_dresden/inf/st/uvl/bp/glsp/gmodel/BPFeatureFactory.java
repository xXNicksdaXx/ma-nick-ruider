/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.bp.glsp.gmodel;

import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.bp.glsp.utils.BTypeUtil;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.UVLFeatureFactory;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.utils.GModelUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Cardinality;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GCompartment;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.*;
import org.eclipse.glsp.graph.util.GConstants;

import java.util.Map;

import static de.tu_dresden.inf.st.uvl.bp.glsp.utils.BTypeUtil.*;

public class BPFeatureFactory extends UVLFeatureFactory {

    @Override
    protected GNode create(final Feature feature) {
        if (isBThread(feature)) {
            return createBThread(feature);
        }


        return createFeature(feature);
    }

    protected GNode createBThread(final Feature feature) {
        UVLModelIndex index = modelState.getIndex();
        String id = index.getIdFor(feature).orElseThrow(
                () -> new IllegalStateException("Feature not indexed: " + feature.getFeatureName())
        );

        GNodeBuilder nodeBuilder = new GNodeBuilder(BPModelTypes.B_THREAD)
                .id(id)
                .layout(GConstants.Layout.VBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(0)
                        .paddingLeft(0)
                        .paddingRight(0)
                        .paddingBottom(0.0)
                        .resizeContainer(true))
                .add(buildBThreadHeader(id, feature.getFeatureName(), feature.getCardinality()))
                .add(buildAttributeCompartment(id, feature))
                .add(buildEventCompartment(id, feature));

        applyNodeData(nodeBuilder, id);
        return nodeBuilder.build();
    }

    protected GCompartment buildBThreadHeader(final String id, final String name, final Cardinality cardinality) {
        GLabel threadLabel = new GLabelBuilder(DefaultTypes.LABEL)
                .text("<<BThread>>")
                .addCssClass("cursive-title")
                .build();
        GLabel headerLabel = new GLabelBuilder(UVLModelTypes.FEATURE_NAME)
                .id(id + "_header_label")
                .addCssClass("bold-title")
                .text(name)
                .build();
        GCompartmentBuilder headerBuilder = new GCompartmentBuilder(DefaultTypes.COMPARTMENT_HEADER)
                .id(id + "_header")
                .layout(GConstants.Layout.VBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(4)
                        .paddingLeft(4)
                        .paddingRight(4)
                        .paddingBottom(4.0)
                        .hAlign(GConstants.HAlign.CENTER)
                        .resizeContainer(true))
                .add(threadLabel)
                .add(headerLabel);

        if (cardinality != null) {
            addCardinality(cardinality, id,  headerBuilder);
        }

        return headerBuilder.build();
    }

    @Override
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
            Attribute<?> attribute = entry.getValue();
            if (isBThreadAttribute(attribute) || isBEventAttribute(attribute)) {
                continue;
            }
            compartmentBuilder.add(createAttribute(id, entry.getKey(), attribute));
        }

        return compartmentBuilder.build();
    }

    protected GCompartment buildEventCompartment(final String id, final Feature feature) {
        GCompartmentBuilder compartmentBuilder = new GCompartmentBuilder(DefaultTypes.COMPARTMENT)
                .id(id + "_event_compartment")
                .layout(GConstants.Layout.VBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(0)
                        .paddingLeft(0)
                        .paddingRight(0)
                        .paddingBottom(0.0)
                        .hAlign(GConstants.HAlign.LEFT)
                        .resizeContainer(true));

        for (Map.Entry<String, Attribute<?>> entry : feature.getAttributes().entrySet()) {
            Attribute<?> attribute = entry.getValue();
            if (isBEventAttribute(attribute)) {
                compartmentBuilder.add(createEvent(id, entry.getKey(), attribute));
            }
        }

        return compartmentBuilder.build();
    }

    public GCompartment createEvent(String id, String eventName, Attribute<?> attribute) {
        BTypeUtil.BEventType eventType = BTypeUtil.getBEventType(attribute);
        String eventId = GModelUtil.appendEventSegment(id, eventName);
        return switch (eventType) {
            case REQUESTED -> createRequestedEvent(eventId, attribute);
            case BLOCKED -> createBlockedEvent(eventId, attribute);
            case WAITED_FOR -> createWaitedForEvent(eventId, attribute);
            default -> throw new IllegalArgumentException("Unknown BEvent type for attribute: " + attribute.getName());
        };
    }

    protected GCompartment createRequestedEvent(String eventId, Attribute<?> attribute) {
        // circle: https://github.com/microsoft/vscode-codicons/blob/main/src/icons/circle-filled.svg
        String d = "M8 4C8.36719 4 8.72135 4.04818 9.0625 4.14453C9.40365 4.23828 9.72135 4.3724 10.0156 4.54688C10.3125 4.72135 10.582 4.93099 10.8242 5.17578C11.069 5.41797 11.2786 5.6875 11.4531 5.98438C11.6276 6.27865 11.7617 6.59635 11.8555 6.9375C11.9518 7.27865 12 7.63281 12 8C12 8.36719 11.9518 8.72135 11.8555 9.0625C11.7617 9.40365 11.6276 9.72266 11.4531 10.0195C11.2786 10.3138 11.069 10.5833 10.8242 10.8281C10.582 11.0703 10.3125 11.2786 10.0156 11.4531C9.72135 11.6276 9.40365 11.763 9.0625 11.8594C8.72135 11.9531 8.36719 12 8 12C7.63281 12 7.27865 11.9531 6.9375 11.8594C6.59635 11.763 6.27734 11.6276 5.98047 11.4531C5.6862 11.2786 5.41667 11.0703 5.17188 10.8281C4.92969 10.5833 4.72135 10.3138 4.54688 10.0195C4.3724 9.72266 4.23698 9.40365 4.14062 9.0625C4.04688 8.72135 4 8.36719 4 8C4 7.63281 4.04688 7.27865 4.14062 6.9375C4.23698 6.59635 4.3724 6.27865 4.54688 5.98438C4.72135 5.6875 4.92969 5.41797 5.17188 5.17578C5.41667 4.93099 5.6862 4.72135 5.98047 4.54688C6.27734 4.3724 6.59635 4.23828 6.9375 4.14453C7.27865 4.04818 7.63281 4 8 4Z";

        GCompartment iconCompartment = new GCompartmentBuilder(DefaultTypes.COMPARTMENT)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(0)
                        .paddingLeft(0)
                        .paddingRight(0)
                        .paddingBottom(0.0)
                        .prefHeight(10)
                        .prefWidth(12)
                        .hAlign(GConstants.HAlign.CENTER)
                        .vAlign(GConstants.VAlign.CENTER))
                .add(new GShapePrerenderedElementBuilder(DefaultTypes.SHAPE_PRE_RENDERED)
                        .addCssClass("bp-event-requested")
                        .code("<path transform='scale(1.5) translate(-4,-4)' d='" + d +"'/>")
                        .build())
                .build();

        return new GCompartmentBuilder(BPModelTypes.REQUESTED_EVENT)
                .id(eventId)
                .layout(GConstants.Layout.HBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(2)
                        .paddingLeft(2)
                        .paddingRight(2)
                        .paddingBottom(2.0)
                        .hAlign(GConstants.HAlign.LEFT)
                        .resizeContainer(true))
                .add(iconCompartment)
                .add(new GLabelBuilder(BPModelTypes.EVENT_NAME)
                        .id(eventId + "_name")
                        .text(attribute.getName())
                        .build())
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .text(" ( ")
                        .build())
                .add(new GLabelBuilder(BPModelTypes.EVENT_PRIORITY)
                        .id(eventId + "_priority")
                        .text(getPriority(attribute))
                        .build())
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .text(")")
                        .build())
                .build();
    }

    protected GCompartment createBlockedEvent(String eventId, Attribute<?> attribute) {
        // triangle: https://github.com/microsoft/vscode-codicons/blob/main/src/icons/debug-breakpoint-function.svg
        String d = "M12.487 11.249L8.43199 4.24899C8.25199 3.93999 7.74599 3.93999 7.56699 4.24899L3.51199 11.249C3.42199 11.404 3.42199 11.595 3.51199 11.75C3.60199 11.904 3.76699 12 3.94599 12H12.055C12.234 12 12.399 11.904 12.489 11.75C12.578 11.595 12.578 11.404 12.489 11.249H12.487Z";

        GCompartment iconCompartment = new GCompartmentBuilder(DefaultTypes.COMPARTMENT)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(0)
                        .paddingLeft(0)
                        .paddingRight(0)
                        .paddingBottom(0.0)
                        .prefHeight(10)
                        .prefWidth(12)
                        .hAlign(GConstants.HAlign.CENTER)
                        .vAlign(GConstants.VAlign.CENTER))
                .add(new GShapePrerenderedElementBuilder(DefaultTypes.SHAPE_PRE_RENDERED)
                        .addCssClass("bp-event-blocked")
                        .code("<path transform='scale(1.5) translate(-4,-4)' d='" + d +"'/>")
                        .build())
                .build();

        return new GCompartmentBuilder(BPModelTypes.BLOCKED_EVENT)
                .id(eventId)
                .layout(GConstants.Layout.HBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(2)
                        .paddingLeft(2)
                        .paddingRight(2)
                        .paddingBottom(2.0)
                        .hAlign(GConstants.HAlign.LEFT)
                        .resizeContainer(true))
                .add(iconCompartment)
                .add(new GLabelBuilder(BPModelTypes.EVENT_NAME)
                        .id(eventId + "_name")
                        .text(attribute.getName())
                        .build())
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .text(" ( ")
                        .build())
                .add(new GLabelBuilder(BPModelTypes.EVENT_PRIORITY)
                        .id(eventId + "_priority")
                        .text(getPriority(attribute))
                        .build())
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .text(")")
                        .build())
                .build();
    }

    protected GCompartment createWaitedForEvent(String eventId, Attribute<?> attribute) {
        GCompartment iconCompartment = new GCompartmentBuilder(DefaultTypes.COMPARTMENT)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(0)
                        .paddingLeft(0)
                        .paddingRight(0)
                        .paddingBottom(0.0)
                        .prefHeight(10)
                        .prefWidth(12)
                        .hAlign(GConstants.HAlign.CENTER)
                        .vAlign(GConstants.VAlign.CENTER))
                .add(new GShapePrerenderedElementBuilder(DefaultTypes.SHAPE_PRE_RENDERED)
                        .addCssClass("bp-event-waited-for")
                        .code("<rect width='12' height='12' />")
                        .build())
                .build();

        return new GCompartmentBuilder(BPModelTypes.WAITED_FOR_EVENT)
                .id(eventId)
                .layout(GConstants.Layout.HBOX)
                .layoutOptions(new GLayoutOptions()
                        .paddingTop(2)
                        .paddingLeft(2)
                        .paddingRight(2)
                        .paddingBottom(2.0)
                        .hAlign(GConstants.HAlign.LEFT)
                        .resizeContainer(true))
                .add(iconCompartment)
                .add(new GLabelBuilder(BPModelTypes.EVENT_NAME)
                        .id(eventId + "_name")
                        .text(attribute.getName())
                        .build())
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .text(" ( ")
                        .build())
                .add(new GLabelBuilder(BPModelTypes.EVENT_PRIORITY)
                        .id(eventId + "_priority")
                        .text(getPriority(attribute))
                        .build())
                .add(new GLabelBuilder(DefaultTypes.LABEL)
                        .text(")")
                        .build())
                .build();
    }

    @SuppressWarnings("unchecked")
    protected String getPriority(Attribute<?> attribute) {
        Object value = attribute.getValue();
        if (value instanceof Map<?, ?> rawMap && !rawMap.isEmpty()) {
            Map<String, Attribute<?>> map = (Map<String, Attribute<?>>) rawMap;
            return map.get("priority").getValue().toString();
        }
        return "Unknown";
    }
}
