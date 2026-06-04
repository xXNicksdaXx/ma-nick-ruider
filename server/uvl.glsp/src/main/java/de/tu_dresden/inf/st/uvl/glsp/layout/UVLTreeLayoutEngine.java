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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.glsp.graph.*;
import org.eclipse.glsp.graph.util.GraphUtil;
import org.eclipse.glsp.server.layout.LayoutEngine;
import org.eclipse.glsp.server.operations.LayoutOperation;

public class UVLTreeLayoutEngine implements LayoutEngine {

  // Configuration for the layout
  private static final double LEVEL_SPACING = 64.0; // Vertical gap between levels
  private static final double SIBLING_GAP = 24.0; // Horizontal gap between nodes
  private static final double EXTRA_LEVEL_PADDING = 8.0; // Extra padding added to each level height

  // Computed per-layout run: cumulative Y offsets for each level (level -> Y)
  private final List<Double> levelOffsets = new ArrayList<>();

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

    // place additional elements (e.g., constraint box)
    postProcessLayout(rootNode);

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

    if (feature.isSubmodelRoot()) {
      return node;
    }

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

    // Compute per-level heights and offsets so variable node heights are respected
    List<Double> maxHeights = computeMaxHeightsPerLevel(root);
    computeLevelOffsets(maxHeights);

    // Pass 1: Calculate initial positions and modifiers (Bottom-Up)
    firstWalk(root, 0);

    // Resolve any remaining overlaps within the same level (helps bottom layer)
    resolveLevelOverlaps(root);

    // Pass 2: Calculate final absolute positions (Top-Down)
    secondWalk(root, 0);

    // Pass 3: Shift entire tree so all coordinates are positive
    normalizeCoordinates(root);
  }

  /** First Walk: Post-order traversal. Computes 'prelim' x-coordinates and 'modifier' values. */
  private void firstWalk(WalkersNode node, int level) {
    // Use precomputed level offsets if available so tall nodes don't collide vertically
    if (level < levelOffsets.size()) {
      node.y = levelOffsets.get(level);
    } else {
      node.y = level * LEVEL_SPACING;
    }

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

      // Calculate the logical center of the children using their temporary absolute X
      double firstAbs = getAbsoluteX(node.children.getFirst());
      double lastAbs = getAbsoluteX(node.children.getLast());
      double midPoint = (firstAbs + lastAbs) / 2.0;

      WalkersNode leftSibling = getLeftSibling(node);
      if (leftSibling != null) {
        // Determine placement based on sibling. We keep prelim as a local-relative
        // value but compute a modifier such that the node's center aligns properly
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

  public void postProcessLayout(final WalkersNode root) {
    // Build an ordered pipeline and place each element from left to right.
    double currentX = getMinimumXOfTree(root);
    double modelBottomY = getMaximumHeightOfTree(root) + LEVEL_SPACING;
    List<GShapeElement> pipeline = buildPostProcessPipeline(root);
    for (GShapeElement shapeElement : pipeline) {
      currentX = positionPostProcessElement(shapeElement, currentX, modelBottomY);
    }
  }

  protected List<GShapeElement> buildPostProcessPipeline(final WalkersNode root) {
    List<GShapeElement> pipeline = new ArrayList<>();
    modelState.getIndex().getGModelElement("constraint_box", GNode.class).ifPresent(pipeline::add);
    return pipeline;
  }

  protected double positionPostProcessElement(
      final GShapeElement shapeElement, final double x, final double y) {
    shapeElement.setPosition(GraphUtil.point(x, y));
    return x + shapeElement.getSize().getWidth() + SIBLING_GAP;
  }

  protected double getMinimumXOfTree(final WalkersNode root) {
    // Minimum absolute left edge among all nodes in the tree.
    double minX = root.x;
    for (WalkersNode child : root.children) {
      minX = Math.min(minX, getMinimumXOfTree(child));
    }
    return minX;
  }

  protected double getMaximumHeightOfTree(final WalkersNode root) {
    // Maximum absolute bottom edge among all nodes in the tree.
    double maxHeight = root.y + root.height;
    for (WalkersNode child : root.children) {
      maxHeight = Math.max(maxHeight, getMaximumHeightOfTree(child));
    }
    return maxHeight;
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

  // Compute maximum heights per depth level so we can lay out levels with variable heights
  private List<Double> computeMaxHeightsPerLevel(WalkersNode root) {
    List<Double> heights = new ArrayList<>();
    collectMaxHeights(root, 0, heights);
    return heights;
  }

  private void collectMaxHeights(WalkersNode node, int depth, List<Double> heights) {
    if (heights.size() <= depth) {
      heights.add(node.height);
    } else {
      heights.set(depth, Math.max(heights.get(depth), node.height));
    }
    for (WalkersNode child : node.children) {
      collectMaxHeights(child, depth + 1, heights);
    }
  }

  private void computeLevelOffsets(List<Double> maxHeights) {
    levelOffsets.clear();
    double y = 0.0;
    for (Double h : maxHeights) {
      levelOffsets.add(y);
      double levelHeight = (h != null ? h : 0.0) + EXTRA_LEVEL_PADDING;
      y += levelHeight + LEVEL_SPACING;
    }
  }

  // Resolve horizontal overlaps that remain within the same level after firstWalk.
  // This pass walks levels left-to-right and shifts nodes that would overlap their
  // predecessor at the same depth.
  private void resolveLevelOverlaps(WalkersNode root) {
    Map<Integer, List<WalkersNode>> byLevel = new HashMap<>();
    collectNodesByLevel(root, 0, byLevel);

    for (Map.Entry<Integer, List<WalkersNode>> e : byLevel.entrySet()) {
      List<WalkersNode> nodes = e.getValue();
      if (nodes.size() < 2) continue;
      // Sort by their temporary absolute X to enforce left-to-right ordering
      nodes.sort(Comparator.comparingDouble(this::getAbsoluteX));

      double prevRight = Double.NEGATIVE_INFINITY;
      for (WalkersNode node : nodes) {
        double left = getAbsoluteX(node) - (node.width / 2.0);
        double right = getAbsoluteX(node) + (node.width / 2.0);

        if (prevRight == Double.NEGATIVE_INFINITY) {
          prevRight = right;
          continue;
        }

        double minLeft = prevRight + SIBLING_GAP;
        if (left < minLeft) {
          double shift = minLeft - left;
          // shift the subtree rooted at this node to the right
          shiftSubtreePrelim(node, shift);
          // update right boundary after shift
          right += shift;
        }

        prevRight = right;
      }
    }
  }

  private void collectNodesByLevel(
      WalkersNode node, int depth, Map<Integer, List<WalkersNode>> map) {
    map.computeIfAbsent(depth, _ -> new ArrayList<>()).add(node);
    for (WalkersNode child : node.children) {
      collectNodesByLevel(child, depth + 1, map);
    }
  }

  private void shiftSubtreePrelim(WalkersNode node, double amount) {
    node.prelim += amount;
    node.modifier += amount;
    // children don't need direct modification because modifiers propagate
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
}
