/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.bp.glsp.model.BPModelState;
import de.tu_dresden.inf.st.uvl.bp.glsp.utils.BTypeUtil;
import de.tu_dresden.inf.st.uvl.glsp.utils.GModelUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.server.gmodel.GModelCreateOperationHandler;
import org.eclipse.glsp.server.operations.CreateNodeOperation;

public class BPCreateEventOperationHandler
    extends GModelCreateOperationHandler<CreateNodeOperation> {

  @Inject protected BPModelState modelState;

  BPCreateEventOperationHandler() {
    super(BPModelTypes.REQUESTED_EVENT, BPModelTypes.BLOCKED_EVENT, BPModelTypes.WAITED_FOR_EVENT);
  }

  @Override
  public Optional<Command> createCommand(final CreateNodeOperation operation) {
    return commandOf(() -> executeCreation(operation));
  }

  protected void executeCreation(final CreateNodeOperation operation) {
    String containerId = operation.getContainerId();
    Feature feature = resolveParentFeature(containerId);
    if (!BTypeUtil.isBThread(feature)) {
      throw new IllegalArgumentException(
          "Events can only be created inside BThreads: " + containerId);
    }

    Map<String, Attribute<?>> featureAttributes = feature.getAttributes();
    Optional<GModelUtil.ResolvedAttribute> existingEvent =
        resolveExistingEvent(feature, containerId);

    String eventName =
        existingEvent
            .map(GModelUtil.ResolvedAttribute::mapKey)
            .orElseGet(() -> generateEventName(featureAttributes));
    int priority = existingEvent.map(this::extractPriority).orElse(1);

    featureAttributes.put(
        eventName,
        createEventAttribute(eventName, feature, operation.getElementTypeId(), priority));
    modelState.updateIndex();
  }

  protected Feature resolveParentFeature(final String containerId) {
    String featureId = Optional.ofNullable(GModelUtil.extractUUID(containerId)).orElse(containerId);
    return modelState
        .getIndex()
        .getUVLObject(featureId, Feature.class)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "No parent feature found for event container ID: " + containerId));
  }

  protected Optional<GModelUtil.ResolvedAttribute> resolveExistingEvent(
      final Feature feature, final String containerId) {
    List<String> path = GModelUtil.extractAttributePath(containerId);
    if (path.isEmpty()) {
      return Optional.empty();
    }

    GModelUtil.ResolvedAttribute resolvedAttribute =
        GModelUtil.resolveAttribute(feature, path)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Could not resolve event from container ID: " + containerId));

    if (!BTypeUtil.isBEventAttribute(resolvedAttribute.attribute())) {
      throw new IllegalArgumentException("Container does not reference a BEvent: " + containerId);
    }
    return Optional.of(resolvedAttribute);
  }

  protected Attribute<Map<String, Attribute<?>>> createEventAttribute(
      final String eventName, final Feature feature, final String eventType, final int priority) {
    Map<String, Attribute<?>> subAttributes = new LinkedHashMap<>();
    subAttributes.put("type", new Attribute<>("type", "BEvent", feature));

    switch (eventType) {
      case BPModelTypes.REQUESTED_EVENT ->
          subAttributes.put("requested", new Attribute<>("requested", true, feature));
      case BPModelTypes.BLOCKED_EVENT ->
          subAttributes.put("blocked", new Attribute<>("blocked", true, feature));
      case BPModelTypes.WAITED_FOR_EVENT ->
          subAttributes.put("waited_for", new Attribute<>("waited_for", true, feature));
      default ->
          throw new IllegalArgumentException(
              "Unsupported BEvent create operation type: " + eventType);
    }

    subAttributes.put("priority", new Attribute<>("priority", priority, feature));
    return new Attribute<>(eventName, subAttributes, feature);
  }

  protected int extractPriority(final GModelUtil.ResolvedAttribute resolvedAttribute) {
    return GModelUtil.asAttributeMap(resolvedAttribute.attribute())
        .map(attributes -> attributes.get("priority"))
        .map(Attribute::getValue)
        .filter(Number.class::isInstance)
        .map(Number.class::cast)
        .map(Number::intValue)
        .orElse(1);
  }

  protected String generateEventName(final Map<String, Attribute<?>> featureAttributes) {
    int index = featureAttributes.size() + 1;
    String name = "Event" + index;
    while (featureAttributes.containsKey(name)) {
      index++;
      name = "Event" + index;
    }
    return name;
  }
}
