/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.utils;

public class TypeCastingUtil {

    public static Object convertStringToBestType(String value) {
        if (value == null) {
            return null;
        }

        String v = value.trim();
        if (v.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (v.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (v.isEmpty()) {
            return "";
        }

        // Whole numbers (no decimal point or exponent)
        if (v.matches("^[+-]?\\d+$")) {
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException ignored) {}
            try {
                return Long.parseLong(v);
            } catch (NumberFormatException ignored) {}
            try {
                return Short.parseShort(v);
            } catch (NumberFormatException ignored) {}
            try {
                return Byte.parseByte(v);
            } catch (NumberFormatException ignored) {}
            try {
                return new java.math.BigInteger(v);
            } catch (Exception ignored) {}
        }

        // Floating point numbers (decimal or exponent)
        if (v.matches("^[+-]?(?:\\d+\\.\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?$") ||
            v.matches("^[+-]?\\d+[eE][+-]?\\d+$")) {
            try {
                return Double.parseDouble(v);
            } catch (NumberFormatException ignored) {}
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException ignored) {}
        }

        // Fallback to original string
        return value;
    }
}
