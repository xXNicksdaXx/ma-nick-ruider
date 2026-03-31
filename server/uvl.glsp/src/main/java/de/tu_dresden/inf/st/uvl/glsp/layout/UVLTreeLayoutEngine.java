/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.layout;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelIndex;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import java.util.Optional;
import org.eclipse.glsp.graph.GDimension;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.util.GraphUtil;
import org.eclipse.glsp.server.layout.LayoutEngine;
import org.eclipse.glsp.server.operations.LayoutOperation;

public class UVLTreeLayoutEngine implements LayoutEngine {

  // Configuration for the layout
  private static final double LEVEL_SPACING = 80.0; // Vertical gap between levels
  private static final double SIBLING_GAP = 20.0; // Horizontal gap between nodes

  @Inject protected UVLModelState modelState;

  /**
   * Compute a layout for the model state and modify the model accordingly.
   *
   * @param layoutOperation The optional operation that triggered this layout.
   */
  @Override
  public void layout(Optional<LayoutOperation> layoutOperation) {
    if (modelState.getFeatureModel() == null || modelState.getRoot() == null) {
      return;
    }

    // Parse all features into WalkersNode tree
    Feature rootFeature = modelState.getFeatureModel().getRootFeature();
    if (rootFeature == null) {
      return;
    }
    UVLModelIndex index = modelState.getIndex();
    WalkersNode rootNode = transformFeature(rootFeature, index);

    // Perform the layout algorithm
    layout(rootNode);

    // Transfer the computed layout back to the model state
    transferLayout(rootNode);

    // Remove all routing points from edges
    removeRoutingPoints();
  }

  private WalkersNode transformFeature(Feature feature, UVLModelIndex index) {
    String featureId =
        index
            .getIdFor(feature)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No ID found for Feature: " + feature.getFeatureName()));
    GNode gNode =
        index
            .getGModelElement(featureId, GNode.class)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No GModelElement found for Feature ID: " + featureId));

    GDimension size = gNode.getSize();
    if (size == null) {
      size = GraphUtil.dimension(64, 32);
    }

    WalkersNode node =
        new WalkersNode(featureId, feature.getFeatureName(), size.getWidth(), size.getHeight());

    for (Group childGroup : feature.getChildren()) {
      childGroup
          .getFeatures()
          .forEach(
              childFeature -> {
                WalkersNode childNode = transformFeature(childFeature, index);
                childNode.parent = node;
                node.children.add(childNode);
              });
    }

    return node;
  }

  public void layout(WalkersNode root) {
    if (root == null) return;

    // Pass 1: Calculate initial positions and modifiers (Bottom-Up)
    firstWalk(root, 0);

    // Pass 2: Calculate final absolute positions (Top-Down)
    secondWalk(root, 0);

    // Pass 3: Shift entire tree so all coordinates are positive
    normalizeCoordinates(root);
  }

  /** First Walk: Post-order traversal. Computes 'prelim' x-coordinates and 'modifier' values. */
  private void firstWalk(WalkersNode node, int level) {
    node.y = level * LEVEL_SPACING;

    if (node.children.isEmpty()) {
      // Leaf node: place next to left sibling
      WalkersNode leftSibling = getLeftSibling(node);
      if (leftSibling != null) {
        node.prelim = leftSibling.prelim + spacing(leftSibling, node);
      } else {
        node.prelim = 0;
      }
    } else {
      // Internal node: process children first
      for (WalkersNode child : node.children) {
        child.parent = node;
        firstWalk(child, level + 1);
      }

      // Calculate the logical center of the children
      double midPoint = (node.children.getFirst().prelim + node.children.getLast().prelim) / 2.0;

      WalkersNode leftSibling = getLeftSibling(node);
      if (leftSibling != null) {
        // Determine placement based on sibling
        node.prelim = leftSibling.prelim + spacing(leftSibling, node);
        node.modifier = node.prelim - midPoint;

        // Solve the "Contour Problem" (Subtree Separation)
        applySubtreeSeparation(node);
      } else {
        // No sibling, just place at children's midpoint
        node.prelim = midPoint;
      }
    }
  }

  /**
   * Second Walk: Pre-order traversal. Converts relative positions (prelim + modifiers) into final
   * absolute X coordinates.
   */
  private void secondWalk(WalkersNode node, double modSum) {
    node.x = node.prelim + modSum;
    for (WalkersNode child : node.children) {
      secondWalk(child, modSum + node.modifier);
    }
  }

  /** Shifts the right subtree to ensure no overlap with the left subtree at any depth. */
  private void applySubtreeSeparation(WalkersNode node) {
    WalkersNode leftSibling = getLeftSibling(node);
    if (leftSibling == null) return;

    // Check for conflicts at every level deeper than the current one
    for (int level = 1; ; level++) {
      WalkersNode rightContourOfLeft = getContourNode(leftSibling, level, true);
      WalkersNode leftContourOfRight = getContourNode(node, level, false);

      // If we ran out of nodes on either side, we are done checking
      if (rightContourOfLeft == null || leftContourOfRight == null) break;

      // Calculate the "apparent" x-coordinates for comparison
      double xLeft = getAbsoluteX(rightContourOfLeft);
      double xRight = getAbsoluteX(leftContourOfRight);

      // Distance required = half of left + half of right + standard gap
      double dist =
          (xLeft + (rightContourOfLeft.width / 2.0))
              - (xRight - (leftContourOfRight.width / 2.0))
              + SIBLING_GAP;

      if (dist > 0) {
        // If there is an overlap (dist > 0), shift the current node
        node.prelim += dist;
        node.modifier += dist;
      }
    }
  }

  /*
   * Transfer the computed layout back to the original sprotty graph.
   */
  protected void transferLayout(final WalkersNode node) {
    transferLayout(modelState.getRoot(), node);
  }

  protected void transferLayout(final GModelElement element, final WalkersNode node) {
    if (element instanceof GGraph gGraph) {
      WalkersNode targetNode = getNodeWithId(node, gGraph.getId());
      if (targetNode != null) {
        gGraph.setPosition(GraphUtil.point(targetNode.x, targetNode.y));
        gGraph.setSize(GraphUtil.dimension(targetNode.width, targetNode.height));
      }
    } else if (element instanceof GNode gNode) {
      WalkersNode targetNode = getNodeWithId(node, gNode.getId());
      if (targetNode != null) {
        gNode.setPosition(GraphUtil.point(targetNode.x, targetNode.y));
        gNode.setSize(GraphUtil.dimension(targetNode.width, targetNode.height));
      }
    }

    if (element.getChildren() != null) {
      for (GModelElement child : element.getChildren()) {
        transferLayout(child, node);
      }
    }
  }

  private void removeRoutingPoints() {
    modelState
        .getIndex()
        .getStream(modelState.getRoot())
        .filter(GEdge.class::isInstance)
        .map(GEdge.class::cast)
        .forEach(edge -> edge.getRoutingPoints().clear());
  }

  // --- Helper Methods ---

  /**
   * Traverses up the tree summing modifiers to find the temporary absolute X. Necessary during the
   * First Walk phase before final X is set.
   */
  private double getAbsoluteX(WalkersNode node) {
    double x = node.prelim;
    WalkersNode curr = node.parent;
    while (curr != null) {
      x += curr.modifier;
      curr = curr.parent;
    }
    return x;
  }

  /**
   * Finds the extreme node at a specific relative depth. rightSide=true -> gets the rightmost node
   * of the left tree rightSide=false -> gets the leftmost node of the right tree
   */
  private WalkersNode getContourNode(WalkersNode node, int relativeLevel, boolean rightSide) {
    if (relativeLevel == 0) return node;
    if (node.children.isEmpty()) return null;

    if (rightSide) {
      return getContourNode(node.children.getLast(), relativeLevel - 1, true);
    } else {
      return getContourNode(node.children.getFirst(), relativeLevel - 1, false);
    }
  }

  private double spacing(WalkersNode left, WalkersNode right) {
    return (left.width / 2.0) + (right.width / 2.0) + SIBLING_GAP;
  }

  private WalkersNode getLeftSibling(WalkersNode node) {
    if (node.parent == null) return null;
    int idx = node.parent.children.indexOf(node);
    return idx > 0 ? node.parent.children.get(idx - 1) : null;
  }

  private WalkersNode getNodeWithId(WalkersNode node, String id) {
    if (node.id.equals(id)) {
      return node;
    }
    for (WalkersNode child : node.children) {
      WalkersNode result = getNodeWithId(child, id);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private void normalizeCoordinates(WalkersNode root) {
    double minX = getMinX(root, Double.MAX_VALUE);
    if (minX < 0) {
      shiftTree(root, Math.abs(minX));
    }
  }

  private double getMinX(WalkersNode node, double currentMin) {
    double min = Math.min(node.x - (node.width / 2), currentMin);
    for (WalkersNode child : node.children) {
      min = Math.min(min, getMinX(child, min));
    }
    return min;
  }

  private void shiftTree(WalkersNode node, double amount) {
    node.x += amount;
    for (WalkersNode child : node.children) {
      shiftTree(child, amount);
    }
  }

  // --- Static Methods ---
  public static boolean requiresLayoutOperation(GModelRoot root) {
    long count =
        root.getChildren().stream()
            .filter(element -> element instanceof GNode)
            .map(element -> (GNode) element)
            .filter(node -> node.getPosition().getX() == 0 && node.getPosition().getY() == 0)
            .count();
    return count > 1;
  }
}
