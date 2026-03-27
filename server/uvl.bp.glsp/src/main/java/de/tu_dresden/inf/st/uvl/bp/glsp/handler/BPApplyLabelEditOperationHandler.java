/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.bp.glsp.handler;

import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLApplyLabelEditOperationHandler;
import de.tu_dresden.inf.st.uvl.glsp.utils.GModelUtil;
import de.tu_dresden.inf.st.uvl.glsp.utils.TypeCastingUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import org.eclipse.glsp.graph.GLabel;

import java.util.Map;

public class BPApplyLabelEditOperationHandler extends UVLApplyLabelEditOperationHandler {

    @Override
    protected void handleFeatureLabelEdit(final GLabel label, final Feature feature, final String newText) {
        switch (label.getType()) {
            case BPModelTypes.EVENT_NAME -> updateAttributeName(label, feature, newText);
            case BPModelTypes.EVENT_PRIORITY -> updateEventPriority(label, feature, newText);
            default -> super.handleFeatureLabelEdit(label, feature, newText);
        }
    }

    protected void updateEventPriority(final GLabel label, final Feature feature, final String newValue) {
        GModelUtil.ResolvedAttribute resolvedAttribute = GModelUtil.resolveAttribute(feature, label.getId())
                .orElseThrow(() -> new IllegalArgumentException("No event found for label ID: " + label.getId()));

        Map<String, Attribute<?>> eventAttributes = GModelUtil.asAttributeMap(resolvedAttribute.attribute())
                .orElseThrow(() -> new IllegalArgumentException("Resolved event does not contain sub attributes: " + resolvedAttribute.attribute().getName()));

        if (!eventAttributes.containsKey("priority")) {
            throw new IllegalArgumentException("Resolved event has no priority attribute: " + resolvedAttribute.attribute().getName());
        }

        label.setText(newValue);
        eventAttributes.put("priority", new Attribute<>("priority", TypeCastingUtil.convertStringToBestType(newValue), feature));
    }
}

