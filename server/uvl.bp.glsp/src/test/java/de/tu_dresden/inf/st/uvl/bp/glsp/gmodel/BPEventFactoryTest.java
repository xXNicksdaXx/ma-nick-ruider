/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.gmodel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.glsp.graph.GCompartment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BPEventFactoryTest {

  private BPEventFactory eventFactory;
  private UVLModelIndex modelIndex;

  @BeforeEach
  void setUp() throws NoSuchFieldException, IllegalAccessException {
    eventFactory = new BPEventFactory();
    UVLModelState modelState = mock(UVLModelState.class);
    modelIndex = mock(UVLModelIndex.class);

    when(modelState.getIndex()).thenReturn(modelIndex);

    Field modelStateField = findField(eventFactory.getClass());
    modelStateField.setAccessible(true);
    modelStateField.set(eventFactory, modelState);
  }

  private Field findField(Class<?> clazz) throws NoSuchFieldException {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField("modelState");
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException("modelState not found in class hierarchy");
  }

  @Test
  void requestedEventCreatesRequestedEventCompartment() {
    Feature parentFeature = new Feature("BThread");
    Map<String, Attribute<?>> eventAttrs = new HashMap<>();
    eventAttrs.put("type", new Attribute<>("type", "BEvent", parentFeature));
    eventAttrs.put("requested", new Attribute<>("requested", true, parentFeature));
    Attribute<Map<String, Attribute<?>>> event =
        new Attribute<>("reqEvent", eventAttrs, parentFeature);

    when(modelIndex.getIdFor(parentFeature)).thenReturn(Optional.of("bthread_1"));

    GCompartment compartment = eventFactory.create(event);

    assertNotNull(compartment);
    assertEquals(BPModelTypes.REQUESTED_EVENT, compartment.getType());
    assertEquals("bthread_1_event[reqEvent]", compartment.getId());
  }

  @Test
  void blockedEventCreatesBlockedEventCompartment() {
    Feature parentFeature = new Feature("BThread");
    Map<String, Attribute<?>> eventAttrs = new HashMap<>();
    eventAttrs.put("type", new Attribute<>("type", "BEvent", parentFeature));
    eventAttrs.put("blocked", new Attribute<>("blocked", true, parentFeature));
    Attribute<Map<String, Attribute<?>>> event =
        new Attribute<>("blkEvent", eventAttrs, parentFeature);

    when(modelIndex.getIdFor(parentFeature)).thenReturn(Optional.of("bthread_1"));

    GCompartment compartment = eventFactory.create(event);

    assertNotNull(compartment);
    assertEquals(BPModelTypes.BLOCKED_EVENT, compartment.getType());
  }

  @Test
  void waitedForEventCreatesWaitedForEventCompartment() {
    Feature parentFeature = new Feature("BThread");
    Map<String, Attribute<?>> eventAttrs = new HashMap<>();
    eventAttrs.put("type", new Attribute<>("type", "BEvent", parentFeature));
    eventAttrs.put("waited_for", new Attribute<>("waited_for", true, parentFeature));
    Attribute<Map<String, Attribute<?>>> event =
        new Attribute<>("wfEvent", eventAttrs, parentFeature);

    when(modelIndex.getIdFor(parentFeature)).thenReturn(Optional.of("bthread_1"));

    GCompartment compartment = eventFactory.create(event);

    assertNotNull(compartment);
    assertEquals(BPModelTypes.WAITED_FOR_EVENT, compartment.getType());
  }

  @Test
  void eventWithoutParentThrowsException() {
    // Cannot create an Attribute without a feature parameter, so this test validates
    // that the BPEventFactory properly handles events
    Feature dummyParent = new Feature("DummyParent");
    Map<String, Attribute<?>> eventAttrs = new HashMap<>();
    eventAttrs.put("type", new Attribute<>("type", "BEvent", dummyParent));
    eventAttrs.put("requested", new Attribute<>("requested", true, dummyParent));
    // Create event but don't simulate it having a feature (orphan state)
    Attribute<Map<String, Attribute<?>>> event =
        new Attribute<>("orphanEvent", eventAttrs, dummyParent);
    event.setFeature(null);

    assertThrows(
        IllegalArgumentException.class,
        () -> eventFactory.create(event),
        "Event without parent should throw IllegalArgumentException");
  }

  @Test
  void eventWithUnindexedParentThrowsException() {
    Feature parentFeature = new Feature("UnindexedBThread");
    Map<String, Attribute<?>> eventAttrs = new HashMap<>();
    eventAttrs.put("type", new Attribute<>("type", "BEvent", parentFeature));
    eventAttrs.put("requested", new Attribute<>("requested", true, parentFeature));
    Attribute<Map<String, Attribute<?>>> event =
        new Attribute<>("reqEvent", eventAttrs, parentFeature);

    when(modelIndex.getIdFor(parentFeature)).thenReturn(Optional.empty());

    assertThrows(
        IllegalStateException.class,
        () -> eventFactory.create(event),
        "Event with unindexed parent should throw IllegalStateException");
  }
}
