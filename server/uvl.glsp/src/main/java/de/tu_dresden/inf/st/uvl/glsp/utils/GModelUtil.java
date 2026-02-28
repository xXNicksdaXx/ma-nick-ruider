/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.utils;

import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GModelElement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GModelUtil {

    /**
     * Finds the parent GModelElement of a given GLabel by extracting the UUID from the label's ID and traversing up the model hierarchy.
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
        throw new IllegalArgumentException("Parent node for label with ID " + label.getId() + " not found.");
    }

    /**
     * Extracts the first UUID found within a given string.
     * @param input The string containing the UUID (e.g., "550e8400-e29b-41d4-a716-446655440000_header")
     * @return The extracted UUID string, or null if no match is found.
     */
    public static String extractUUID(String input) {
        if (input == null) return null;

        // Regex pattern for a standard UUID (8-4-4-4-12 hex digits)
        // CASE_INSENSITIVE handles both uppercase and lowercase hex
        String regex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }

        if (matches.isEmpty()) return null;
        return String.join("_", matches);
    }

    /**
     * Extracts the attribute index from a given string.
     * @param input The string containing the attribute index (e.g., "550e8400-e29b-41d4-a716-446655440000_attribute_0_name")
     * @return The extracted attribute index as an integer, or -1 if no valid index is found.
     */
    public static int extractAttributeIndex(String input) {
        if (input == null) return -1;

        // Regex pattern to match "attribute_{index}", "attribute_{index}_name" or "attribute_{index}_value"
        String regex = "attribute_(\\d+)(?:_name|_value)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String indexString = matcher.group(1);
            try {
                return Integer.parseInt(indexString);
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }
}
