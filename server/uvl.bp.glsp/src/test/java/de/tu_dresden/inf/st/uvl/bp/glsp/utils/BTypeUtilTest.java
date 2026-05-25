/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.utils;

import static org.junit.jupiter.api.Assertions.*;

import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.BPFeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BTypeUtilTest {

  @Test
  void isConfigFeatureDetectsConfigCorrectly() {
    Feature configFeature = new Feature("Config");
    configFeature.getAttributes().put("type", new Attribute<>("type", "Config", configFeature));

    assertTrue(
        BTypeUtil.isConfigFeature(configFeature),
        "Feature named Config with type=Config should be config");
  }

  @Test
  void isEnvFeatureDetectsEnvCorrectly() {
    Feature envFeature = new Feature("Env");
    envFeature.getAttributes().put("type", new Attribute<>("type", "Env", envFeature));

    assertTrue(BTypeUtil.isEnvFeature(envFeature), "Feature named Env with type=Env should be env");
  }

  @Test
  void isBThreadDetectsBThreadCorrectly() {
    Feature bThreadFeature = new Feature("MyBThread");
    bThreadFeature.getAttributes().put("type", new Attribute<>("type", "BThread", bThreadFeature));

    assertTrue(
        BTypeUtil.isBThread(bThreadFeature),
        "Feature with type=BThread should be detected as BThread");
  }

  @Test
  void isBThreadAttributeDetectsCorrectly() {
    Feature dummyFeature = new Feature("DummyFeature");
    Attribute<String> bThreadAttribute = new Attribute<>("type", "BThread", dummyFeature);

    assertTrue(
        BTypeUtil.isBThreadAttribute(bThreadAttribute),
        "Attribute with value=BThread should be BThread attribute");
  }

  @Test
  void isBEventAttributeDetectsBEventCorrectly() {
    Feature dummyFeature = new Feature("DummyFeature");
    Map<String, Attribute<?>> eventAttributes = new HashMap<>();
    eventAttributes.put("type", new Attribute<>("type", "BEvent", dummyFeature));
    eventAttributes.put("requested", new Attribute<>("requested", true, dummyFeature));
    Attribute<Map<String, Attribute<?>>> validBEvent =
        new Attribute<>("myEvent", eventAttributes, dummyFeature);

    assertTrue(
        BTypeUtil.isBEventAttribute(validBEvent),
        "Attribute with BEvent type should be detected as B-Event");
  }

  @Test
  void getBEventTypeReturnsCorrectEventType() {
    Feature dummyFeature = new Feature("DummyFeature");
    Map<String, Attribute<?>> requestedEventAttrs = new HashMap<>();
    requestedEventAttrs.put("type", new Attribute<>("type", "BEvent", dummyFeature));
    requestedEventAttrs.put("requested", new Attribute<>("requested", true, dummyFeature));
    Attribute<Map<String, Attribute<?>>> requestedEvent =
        new Attribute<>("req", requestedEventAttrs, dummyFeature);

    assertEquals(
        "comp:requested-event",
        BTypeUtil.getBEventType(requestedEvent),
        "Requested event should return correct type");
  }

  @Test
  void givenFeatureNamedConfigWithoutTypeAttribute_whenIsConfigFeature_thenReturnsFalse() {
    // Arrange
    Feature configFeature = new Feature("Config");

    // Act
    boolean result = BTypeUtil.isConfigFeature(configFeature);

    // Assert
    assertFalse(result);
  }

  @Test
  void givenAttributeValueNotAMap_whenIsBEventAttribute_thenReturnsFalse() {
    // Arrange
    Feature dummyFeature = new Feature("DummyFeature");
    Attribute<String> invalidAttribute = new Attribute<>("event", "not-a-map", dummyFeature);

    // Act
    boolean result = BTypeUtil.isBEventAttribute(invalidAttribute);

    // Assert
    assertFalse(result);
  }

  @Test
  void givenBEventAttributeWithRequestedAndBlockedFlags_whenGetBEventType_thenPrefersRequested() {
    // Arrange
    Feature dummyFeature = new Feature("DummyFeature");
    Map<String, Attribute<?>> attrs = new HashMap<>();
    attrs.put("type", new Attribute<>("type", "BEvent", dummyFeature));
    attrs.put("requested", new Attribute<>("requested", true, dummyFeature));
    attrs.put("blocked", new Attribute<>("blocked", true, dummyFeature));
    Attribute<Map<String, Attribute<?>>> event = new Attribute<>("event", attrs, dummyFeature);

    // Act
    String eventType = BTypeUtil.getBEventType(event);

    // Assert
    assertEquals("comp:requested-event", eventType);
  }

  @Test
  void givenFeatureModelWithMixedFeatures_whenGetAllBThreads_thenReturnsOnlyBThreads() {
    // Arrange
    Feature bThread = new Feature("ThreadA");
    bThread.getAttributes().put("type", new Attribute<>("type", "BThread", bThread));
    Feature normal = new Feature("FeatureA");
    normal.getAttributes().put("type", new Attribute<>("type", "Feature", normal));
    BPFeatureModel featureModel = new BPFeatureModel();
    featureModel.getFeatureMap().put("thread", bThread);
    featureModel.getFeatureMap().put("normal", normal);

    // Act
    var result = BTypeUtil.getAllBThreads(featureModel);

    // Assert
    assertEquals(1, result.size());
    assertTrue(result.contains(bThread));
  }

  @Test
  void
      givenFeatureModelWithMatchingEventAttribute_whenGetAllBThreadsWithBEvent_thenFiltersByEventName() {
    // Arrange
    String eventName = "startEvent";
    Feature matchingThread = new Feature("ThreadMatch");
    matchingThread.getAttributes().put("type", new Attribute<>("type", "BThread", matchingThread));
    Map<String, Attribute<?>> eventAttrs = new HashMap<>();
    eventAttrs.put("type", new Attribute<>("type", "BEvent", matchingThread));
    Attribute<Map<String, Attribute<?>>> eventAttribute =
        new Attribute<>(eventName, eventAttrs, matchingThread);
    matchingThread.getAttributes().put(eventName, eventAttribute);

    Feature otherThread = new Feature("ThreadOther");
    otherThread.getAttributes().put("type", new Attribute<>("type", "BThread", otherThread));

    BPFeatureModel featureModel = new BPFeatureModel();
    featureModel.getFeatureMap().put("match", matchingThread);
    featureModel.getFeatureMap().put("other", otherThread);

    // Act
    var result = BTypeUtil.getAllBThreadsWithBEvent(featureModel, eventName);

    // Assert
    assertEquals(1, result.size());
    assertTrue(result.contains(matchingThread));
  }
}
