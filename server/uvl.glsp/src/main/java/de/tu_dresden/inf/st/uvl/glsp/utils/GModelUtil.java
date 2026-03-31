/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.utils;

import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GModelElement;

public final class GModelUtil {
  private static final Pattern ATTRIBUTE_OR_EVENT_SEGMENT_PATTERN =
      Pattern.compile("_(attribute|event)\\[([^]]+)]");

  private GModelUtil() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Finds the parent GModelElement of a given GLabel by extracting the UUID from the label's ID and
   * traversing up the model hierarchy.
   *
   * @param label The GLabel for which to find the parent element.
   * @return The parent GModelElement corresponding to the extracted UUID.
   */
  public static GModelElement findParent(final GLabel label) {
    // remove "_label"
    String elementId = extractUUID(label.getId());
    if (elementId == null) {
      throw new IllegalArgumentException("No UUID found in label ID: " + label.getId());
    }

    // Traverse up the GModel to find the parent element
    GModelElement parent = label.getParent();
    while (parent != null) {
      if (parent.getId().equals(elementId)) {
        return parent;
      }
      parent = parent.getParent();
    }
    throw new IllegalArgumentException(
        "Parent node for label with ID " + label.getId() + " not found.");
  }

  /**
   * Extracts the first UUID found within a given string.
   *
   * @param input The string containing the UUID (e.g.,
   *     "550e8400-e29b-41d4-a716-446655440000_header")
   * @return The extracted UUID string, or null if no match is found.
   */
  public static String extractUUID(String input) {
    if (input == null) {
      return null;
    }

    // Regex pattern for a standard UUID (8-4-4-4-12 hex digits)
    // CASE_INSENSITIVE handles both uppercase and lowercase hex
    String regex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);

    List<String> matches = new ArrayList<>();
    while (matcher.find()) {
      matches.add(matcher.group());
    }

    if (matches.isEmpty()) {
      return null;
    }
    return String.join("_", matches);
  }

  /**
   * Extracts a name-based attribute/event path from a potentially nested element id. Example:
   * "<featureId>_attribute[name]_attribute[nested]_name" -> ["name", "nested"].
   */
  public static List<String> extractAttributePath(final String input) {
    if (input == null) {
      return Collections.emptyList();
    }

    Matcher matcher = ATTRIBUTE_OR_EVENT_SEGMENT_PATTERN.matcher(input);
    List<String> path = new ArrayList<>();
    while (matcher.find()) {
      path.add(decodeIdSegment(matcher.group(2)));
    }
    return path;
  }

  public static String appendAttributeSegment(final String parentId, final String attributeName) {
    return parentId + "_attribute[" + encodeIdSegment(attributeName) + "]";
  }

  public static String appendEventSegment(final String parentId, final String eventName) {
    return parentId + "_event[" + encodeIdSegment(eventName) + "]";
  }

  public static String encodeIdSegment(final String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  public static String decodeIdSegment(final String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  public static Optional<ResolvedAttribute> resolveAttribute(
      final Feature feature, final String elementId) {
    return resolveAttribute(feature, extractAttributePath(elementId));
  }

  public static Optional<ResolvedAttribute> resolveAttribute(
      final Feature feature, final List<String> path) {
    if (feature == null || path == null || path.isEmpty()) {
      return Optional.empty();
    }

    Map<String, Attribute<?>> currentMap = feature.getAttributes();
    Attribute<?> currentAttribute = null;
    String currentKey = null;

    for (int depth = 0; depth < path.size(); depth++) {
      String name = path.get(depth);
      currentAttribute = currentMap.get(name);
      if (currentAttribute == null) {
        return Optional.empty();
      }
      currentKey = name;

      if (depth < path.size() - 1) {
        Optional<Map<String, Attribute<?>>> nextMap = asAttributeMap(currentAttribute);
        if (nextMap.isEmpty()) {
          return Optional.empty();
        }
        currentMap = nextMap.get();
      }
    }

    return Optional.of(
        new ResolvedAttribute(currentAttribute, currentMap, currentKey, List.copyOf(path)));
  }

  @SuppressWarnings("unchecked")
  public static Optional<Map<String, Attribute<?>>> asAttributeMap(final Attribute<?> attribute) {
    if (attribute == null || !(attribute.getValue() instanceof Map<?, ?> rawMap)) {
      return Optional.empty();
    }
    for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
      if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof Attribute)) {
        return Optional.empty();
      }
    }
    return Optional.of((Map<String, Attribute<?>>) rawMap);
  }

  public record ResolvedAttribute(
      Attribute<?> attribute,
      Map<String, Attribute<?>> parentMap,
      String mapKey,
      List<String> path) {}
}
