/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.eclipse.glsp.graph.util.GConstants;
import org.junit.jupiter.api.Test;

class UVLUtilityClassesTest {

  @Test
  void typeCastingUtilityConvertsCommonScalarTypes() {
    assertNull(TypeCastingUtil.convertStringToBestType(null));
    assertEquals("", TypeCastingUtil.convertStringToBestType("   "));
    assertEquals(Boolean.TRUE, TypeCastingUtil.convertStringToBestType("true"));
    assertEquals(Boolean.FALSE, TypeCastingUtil.convertStringToBestType("FALSE"));
    assertEquals(42, TypeCastingUtil.convertStringToBestType("42"));
    assertEquals(3.5d, TypeCastingUtil.convertStringToBestType("3.5"));
    assertEquals(1000.0d, TypeCastingUtil.convertStringToBestType("1e3"));
    assertEquals("abc", TypeCastingUtil.convertStringToBestType("abc"));
  }

  @Test
  void groupUtilityConvertsTypesAndBuildsNames() {
    Feature parent = new Feature("Root");
    Feature child = new Feature("Child");
    Group group = new Group(Group.GroupType.GROUP_CARDINALITY);
    group.setParentFeature(parent);
    group.getFeatures().add(child);

    assertEquals("Root_group_cardinality", GroupUtil.getGroupName(group));
    assertEquals("Root_group_cardinality_Child", GroupUtil.getEdgeName(group, child));
    assertEquals("Root_group_cardinality", GroupUtil.getEdgeName(group, new Feature("Other")));
    assertEquals(
        UVLModelTypes.OPTIONAL, GroupUtil.convertGroupTypeToModelType(Group.GroupType.OPTIONAL));
    assertEquals(Group.GroupType.OR, GroupUtil.convertModelTypeToGroupType(UVLModelTypes.OR));
    assertThrows(
        IllegalArgumentException.class, () -> GroupUtil.convertModelTypeToGroupType("unknown"));
  }

  @Test
  void gModelUtilityEncodesPathsAndResolvesNestedAttributes() {
    Feature feature = new Feature("Feature A");

    Attribute<Map<String, Attribute<?>>> nested =
        new Attribute<>("nested", new LinkedHashMap<>(), feature);
    nested.getValue().put("inner value", new Attribute<>("inner value", 7, feature));
    feature.getAttributes().put("top level", nested);

    String encoded = GModelUtil.appendAttributeSegment("feature-id", "top level");
    assertEquals("feature-id_attribute[top+level]", encoded);
    assertEquals(List.of("top level"), GModelUtil.extractAttributePath(encoded));
    assertEquals(
        "feature-id_event[event+name]", GModelUtil.appendEventSegment("feature-id", "event name"));
    assertEquals("event name", GModelUtil.decodeIdSegment("event%20name"));
    assertEquals(
        "123e4567-e89b-12d3-a456-426614174000",
        GModelUtil.extractUUID("feature-id_123e4567-e89b-12d3-a456-426614174000_label"));
    assertNull(GModelUtil.extractUUID("no-uuid-here"));

    GModelUtil.ResolvedAttribute resolved =
        GModelUtil.resolveAttribute(feature, List.of("top level", "inner value")).orElseThrow();
    @SuppressWarnings("unchecked")
    Map<String, Attribute<?>> resolvedChildren =
        (Map<String, Attribute<?>>) feature.getAttributes().get("top level").getValue();
    assertSame(resolved.attribute(), resolvedChildren.get("inner value"));
    assertEquals(List.of("top level", "inner value"), resolved.path());
    assertEquals("inner value", resolved.mapKey());

    assertTrue(GModelUtil.resolveAttribute(feature, List.of("missing")).isEmpty());
    assertTrue(GModelUtil.asAttributeMap(new Attribute<>("plain", 1, feature)).isEmpty());
  }

  @Test
  void gModelUtilityCanFindTheParentElementOfALabel() {
    String parentId = "550e8400-e29b-41d4-a716-446655440000";
    GNode parent = new GNodeBuilder("node").id(parentId).layout(GConstants.Layout.VBOX).build();
    GLabel label = new GLabelBuilder("label").id(parentId + "_label").text("Feature").build();
    parent.getChildren().add(label);

    assertSame(parent, GModelUtil.findParent(label));
  }

  @Test
  void gModelUtilityRejectsLabelsWithoutMatchingParents() {
    GLabel orphan = new GLabelBuilder("label").id("no-uuid_label").text("Feature").build();
    assertThrows(IllegalArgumentException.class, () -> GModelUtil.findParent(orphan));
  }
}
