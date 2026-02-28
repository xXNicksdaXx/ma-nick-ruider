/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.layout;

import java.util.ArrayList;
import java.util.List;

public class WalkersNode {
    String id;
    String label;
    double width;
    double height;

    // Algorithm-specific fields
    double x, y;
    double prelim, modifier;
    WalkersNode parent;
    List<WalkersNode> children = new ArrayList<>();

    public WalkersNode(String id, String label, double width, double height) {
        this.id = id;
        this.label = label;
        this.width = width;
        this.height = height;
    }
}
