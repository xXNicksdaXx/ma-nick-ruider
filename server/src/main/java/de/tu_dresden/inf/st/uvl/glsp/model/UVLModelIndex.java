/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.model;

import de.vill.model.Feature;
import de.vill.model.FeatureModel;
import de.vill.model.Group;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.impl.GModelIndexImpl;
import org.eclipse.glsp.graph.util.RootAdapterUtil;
import org.eclipse.glsp.server.utils.BiIndex;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static de.tu_dresden.inf.st.uvl.glsp.utils.ConstraintUtil.convertConstraintTypeToModelType;
import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil.getAllGroups;
import static de.tu_dresden.inf.st.uvl.glsp.utils.GroupUtil.convertGroupTypeToModelType;

public class UVLModelIndex extends GModelIndexImpl {

    protected BiIndex<String, Object> uvlIndex;

    protected UVLModelIndex(final EObject target) {
        super(target);
        this.uvlIndex = new BiIndex<>();
    }

    @Override
    public boolean isAdapterForType(final Object type) {
        return super.isAdapterForType(type) || UVLModelIndex.class.equals(type);
    }

    @Override
    public void clear() {
        super.clear();
        this.uvlIndex.clear();
    }

    protected void indexFeatureModel(final FeatureModel featureModel) {
        this.uvlIndex.clear();

        // Index FeatureModel based on the GModelIndex
        Collection<GModelElement> modelElements = idToElement.values();

        featureModel.getFeatureMap().values().forEach(feature -> indexFeature(feature, modelElements));
        getAllGroups(featureModel).forEach(group -> indexGroup(group, modelElements));
        featureModel.getOwnConstraints().forEach(constraint -> indexConstraint(constraint, modelElements));
    }

    private void indexFeature(final Feature feature, Collection<GModelElement> modelElements) {
        // find the corresponding GModelElement for the Feature
        Optional<String> matchingNodeId = modelElements.stream()
                .filter(GNode.class::isInstance)
                .map(GNode.class::cast)
                .filter(node -> {
                    // check children for label with feature name
                    Optional<GLabel> labelElement = node.getChildren().stream()
                            .flatMap(element -> element.getChildren().stream())
                            .filter(GLabel.class::isInstance)
                            .map(GLabel.class::cast)
                            .filter(label -> {
                                String labelText = label.getText();
                                return labelText != null && labelText.equals(feature.getFeatureName());
                            })
                            .findFirst();
                    return labelElement.isPresent();
                })
                .map(GNode::getId)
                .findAny();

        if (matchingNodeId.isPresent()) {
            String id = matchingNodeId.get();
            uvlIndex.put(id, feature);
        } else {
            String uuid = UUID.randomUUID().toString();
            uvlIndex.putIfAbsent(uuid, feature);
        }
    }

    private void indexGroup(final Group group, Collection<GModelElement> modelElements) {
        // Find the corresponding GModelElement for the Group
        Optional<String> matchingGroupId = modelElements.stream()
                .filter(GEdge.class::isInstance)
                .map(GEdge.class::cast)
                .filter(edge -> {
                    // check for type
                    String expectedType = convertGroupTypeToModelType(group.GROUPTYPE);
                    return edge.getType().equals(expectedType);
                })
                .filter(edge -> {
                    // check for identical source node
                    Optional<String> id = getIdFor(group.getParentFeature());
                    return id.isPresent() && edge.getSourceId().equals(id.get());
                })
                .map(GEdge::getId)
                .map(id -> id.substring(0, id.indexOf("_"))) // remove feature part
                .findAny();

        if (matchingGroupId.isPresent()) {
            String id = matchingGroupId.get();
            uvlIndex.putIfAbsent(id, group);
        } else {
            String uuid = UUID.randomUUID().toString();
            uvlIndex.putIfAbsent(uuid, group);
        }
    }

    private void indexConstraint(final Constraint constraint, Collection<GModelElement> modelElements) {
        // Find the corresponding GModelElement for the Constraint
        Optional<String> matchingConstraintId = modelElements.stream()
                .filter(GEdge.class::isInstance)
                .map(GEdge.class::cast)
                .filter(edge -> {
                    // check for type
                    String expectedType = convertConstraintTypeToModelType(constraint);
                    return edge.getType().equals(expectedType);
                })
                .filter(edge -> {
                    // check for identical source and target nodes
                    Optional<String> sourceId = getIdFor(((LiteralConstraint) constraint.getConstraintSubParts().getFirst()).getFeature());
                    Optional<String> targetId = getIdFor(((LiteralConstraint) constraint.getConstraintSubParts().getLast()).getFeature());
                    return sourceId.isPresent() && targetId.isPresent() && edge.getSourceId().equals(sourceId.get()) && edge.getTargetId().equals(targetId.get());
                })
                .map(GEdge::getId)
                .findAny();

        if (matchingConstraintId.isPresent()) {
            String id = matchingConstraintId.get();
            uvlIndex.putIfAbsent(id, constraint);
        } else {
            String uuid = UUID.randomUUID().toString();
            uvlIndex.putIfAbsent(uuid, constraint);
        }
    }

    public Optional<Object> getUVLObject(final String id) {
        return Optional.ofNullable(uvlIndex.get(id));
    }

    public <T> Optional<T> getUVLObject(final String id, final Class<T> clazz) {
        return safeCast(Optional.ofNullable(uvlIndex.get(id)), clazz);
    }

    public Optional<Object> getUVLObject(final GModelElement gModelElement) {
        return getUVLObject(gModelElement.getId());
    }

    public <T> Optional<T> getUVLObject(final GModelElement gModelElement, final Class<T> clazz) {
        return getUVLObject(gModelElement.getId(), clazz);
    }

    public Optional<GModelElement> getGModelElement(final String id) {
        return get(id);
    }

    public <T extends GModelElement> Optional<T> getGModelElement(final String id, final Class<T> clazz) {
        return safeCast(get(id), clazz);
    }

    public Optional<GModelElement> getGModelElement(final Object object) {
        return getGModelElement(uvlIndex.getKey(object));
    }

    public <T extends GModelElement> Optional<T> getGModelElement(final Object object, final Class<T> clazz) {
        return getGModelElement(uvlIndex.getKey(object), clazz);
    }

    public Optional<String> getIdFor(final Object object) {
        return Optional.ofNullable(uvlIndex.getKey(object));
    }

    public Set<String> allFeatureModelIds() {
        return this.uvlIndex.map().keySet();
    }

    public Set<String> allGModelIds() {
        return allIds();
    }

    protected <T> Optional<T> safeCast(final Optional<?> toCast, final Class<T> clazz) {
        return toCast.filter(clazz::isInstance).map(clazz::cast);
    }

    public static UVLModelIndex getOrCreate(final GModelElement element) {
        return RootAdapterUtil.getOrCreate(element, UVLModelIndex::new, UVLModelIndex.class);
    }
}
