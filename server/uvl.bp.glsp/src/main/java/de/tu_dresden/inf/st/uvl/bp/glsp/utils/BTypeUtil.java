/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.utils;

import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import java.util.Map;

public class BTypeUtil {
  public static boolean isBThread(final Feature feature) {
    return feature.getAttributes().values().stream().anyMatch(BTypeUtil::isBThreadAttribute);
  }

  public static boolean isBThreadAttribute(final Attribute<?> attribute) {
    return attribute.getName().equals("type") && attribute.getValue().equals("BThread");
  }

  @SuppressWarnings("unchecked")
  public static boolean isBEventAttribute(final Attribute<?> attribute) {
    Object value = attribute.getValue();
    if (!(value instanceof Map<?, ?> rawMap)) {
      return false;
    }

    for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
      if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof Attribute)) {
        return false;
      }
    }

    Map<String, Attribute<?>> subAttributes = (Map<String, Attribute<?>>) rawMap;
    Attribute<?> typeAttribute = subAttributes.get("type");
    if (typeAttribute == null) {
      return false;
    }

    return typeAttribute.getName().equals("type") && typeAttribute.getValue().equals("BEvent");
  }

  @SuppressWarnings("unchecked")
  public static String getBEventType(final Attribute<?> attribute) {
    if (!isBEventAttribute(attribute)) {
      return "";
    }

    Map<String, Attribute<?>> subAttributes = (Map<String, Attribute<?>>) attribute.getValue();

    if (subAttributes.containsKey("requested")
        && subAttributes.get("requested").getValue().equals(true)) {
      return BPModelTypes.REQUESTED_EVENT;
    } else if (subAttributes.containsKey("blocked")
        && subAttributes.get("blocked").getValue().equals(true)) {
      return BPModelTypes.BLOCKED_EVENT;
    } else if (subAttributes.containsKey("waited_for")
        && subAttributes.get("waited_for").getValue().equals(true)) {
      return BPModelTypes.WAITED_FOR_EVENT;
    } else {
      return "";
    }
  }
}
