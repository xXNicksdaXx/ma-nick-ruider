/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.bp.glsp.handler;

import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.bp.glsp.utils.BTypeUtil;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLApplyLabelEditOperationHandler;
import de.tu_dresden.inf.st.uvl.glsp.utils.GModelUtil;
import de.tu_dresden.inf.st.uvl.glsp.utils.TypeCastingUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import org.eclipse.glsp.graph.GLabel;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BPApplyLabelEditOperationHandler extends UVLApplyLabelEditOperationHandler {

    @Override
    protected void handleFeatureLabelEdit(final GLabel label, final Feature feature, final String newText) {
        switch (label.getType()) {
            case BPModelTypes.EVENT_NAME -> updateEventName(label, feature, newText);
            case BPModelTypes.EVENT_PRIORITY -> updateEventPriority(label, feature, newText);
            default -> super.handleFeatureLabelEdit(label, feature, newText);
        }
    }

    protected void updateEventName(final GLabel label, final Feature feature, final String newText) {
        GModelUtil.ResolvedAttribute resolvedAttribute = GModelUtil.resolveAttribute(feature, label.getId())
                .orElseThrow(() -> new IllegalArgumentException("No attribute found for label ID: " + label.getId()));

        // Check if this is a b-thread event using proper type detection
        if (!BTypeUtil.isBEventAttribute(resolvedAttribute.attribute())) {
            throw new IllegalArgumentException("Attribute is not a b-thread event: " + resolvedAttribute.attribute().getName());
        }

        Map<String, Attribute<?>> eventAttributes = GModelUtil.asAttributeMap(resolvedAttribute.attribute())
                .orElseThrow(() -> new IllegalArgumentException("Event attribute cannot be mapped to attribute map: " + resolvedAttribute.attribute().getName()));

        handleEventNameUpdate(label, feature, resolvedAttribute, eventAttributes, newText);
    }

    protected void handleEventNameUpdate(final GLabel label,
                                         final Feature feature,
                                         final GModelUtil.ResolvedAttribute resolvedAttribute,
                                         final Map<String, Attribute<?>> eventAttributes,
                                         final String newText) {
        // Parse event name to extract name and potential priority
        EventNamePriority parsed = parseEventNameAndPriority(newText);

        if (resolvedAttribute.parentMap().containsKey(parsed.name()) && !Objects.equals(parsed.name(), resolvedAttribute.mapKey())) {
            throw new IllegalArgumentException("An event with the name '" + parsed.name() + "' already exists at this level.");
        }

        // Manage priority attribute based on parsed priority value
        if (parsed.priority() != null && parsed.priority() > 0) {
            // Add or update priority attribute
            eventAttributes.put("priority", new Attribute<>("priority", parsed.priority(), feature));
        } else if (parsed.priority() != null) {
            // Remove priority attribute if priority is 0 or negative
            eventAttributes.remove("priority");
        }

        // Update GModel
        label.setText(parsed.name());

        // Update Feature Attribute with clean name (no priority suffix)
        Attribute<?> newAttribute = new Attribute<>(parsed.name(), eventAttributes, feature);
        replaceMapEntry(resolvedAttribute.parentMap(), resolvedAttribute.mapKey(), parsed.name(), newAttribute);
    }

    /**
     * Parse an event name that may contain a priority suffix in brackets.
     * Format: "eventName [priority]" or just "eventName"
     * 
     * @param text the event name text, potentially with priority suffix
     * @return EventNamePriority record with name and optional priority
     */
    protected EventNamePriority parseEventNameAndPriority(final String text) {
        Pattern pattern = Pattern.compile("^(.+?)\\s*\\[(-?\\d+)]$");
        Matcher matcher = pattern.matcher(text.trim());

        if (matcher.matches()) {
            String name = matcher.group(1).trim();
            int priority = Integer.parseInt(matcher.group(2));
            return new EventNamePriority(name, priority);
        }

        // No priority suffix found, use the text as-is
        return new EventNamePriority(text.trim(), null);
    }

    protected void updateEventPriority(final GLabel label, final Feature feature, final String newValue) {
        GModelUtil.ResolvedAttribute resolvedAttribute = GModelUtil.resolveAttribute(feature, label.getId())
                .orElseThrow(() -> new IllegalArgumentException("No event found for label ID: " + label.getId()));

        Map<String, Attribute<?>> eventAttributes = GModelUtil.asAttributeMap(resolvedAttribute.attribute())
                .orElseThrow(() -> new IllegalArgumentException("Resolved event does not contain sub attributes: " + resolvedAttribute.attribute().getName()));

        String trimmedValue = newValue.trim();
        if (trimmedValue.isEmpty()) {
            label.setText("");
            eventAttributes.remove("priority");
            return;
        }

        Object convertedValue = TypeCastingUtil.convertStringToBestType(trimmedValue);
        if (convertedValue instanceof String) {
            // Priority must be numeric; ignore non-empty string edits.
            return;
        }

        if (convertedValue instanceof Number number && number.intValue() <= 0) {
            label.setText("");
            eventAttributes.remove("priority");
            return;
        }

        label.setText(trimmedValue);
        eventAttributes.put("priority", new Attribute<>("priority", convertedValue, feature));
    }

    /**
     * Record to represent parsed event name with optional priority.
     */
    protected record EventNamePriority(String name, Integer priority) {}
}

