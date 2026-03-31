/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.handler;

import static de.tu_dresden.inf.st.uvl.glsp.utils.GroupUtil.convertModelTypeToGroupType;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.server.gmodel.GModelCreateOperationHandler;
import org.eclipse.glsp.server.operations.CreateEdgeOperation;

public class UVLCreateRelationEdgeOperationHandler
    extends GModelCreateOperationHandler<CreateEdgeOperation> {

  @Inject protected UVLModelState modelState;

  UVLCreateRelationEdgeOperationHandler() {
    super(
        UVLModelTypes.MANDATORY,
        UVLModelTypes.OPTIONAL,
        UVLModelTypes.ALTERNATIVE,
        UVLModelTypes.OR);
  }

  UVLCreateRelationEdgeOperationHandler(final String... elementTypeIds) {
    super(elementTypeIds);
  }

  @Override
  public Optional<Command> createCommand(CreateEdgeOperation operation) {
    return edgeAlreadyExists(operation, operation.getElementTypeId())
        ? doNothing()
        : commandOf(() -> executeCreation(operation));
  }

  protected void executeCreation(CreateEdgeOperation operation) {
    Feature sourceFeature = getSourceFeature(operation);
    Feature targetFeature = getTargetFeature(operation);

    // check for an existing edge between the two features (must be replaced)
    Optional<GEdge> existingEdge = findExistingEdge(operation);
    existingEdge.ifPresent(gEdge -> removeExistingEdge(sourceFeature, targetFeature, gEdge));

    // create or update group for mandatory relationship
    Group.GroupType groupType = getGroupType(operation);
    Optional<Group> existingGroup = findExistingGroup(sourceFeature, groupType);
    existingGroup.ifPresentOrElse(
        // update: add target feature to existing group
        group -> group.getFeatures().add(targetFeature),
        // create new group and add target feature
        () -> createNewGroup(sourceFeature, targetFeature, groupType));

    // update the model index
    modelState.updateIndex();
  }

  protected void removeExistingEdge(Feature source, Feature target, GEdge gEdge) {
    // Get the existing group related to the GEdge
    Group.GroupType existingType = convertModelTypeToGroupType(gEdge.getType());
    Group existingGroup =
        source.getChildren().stream()
            .filter(group -> group.GROUPTYPE == existingType)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Existing group not found for type: " + existingType));

    // Remove the target feature from the existing group
    existingGroup.getFeatures().remove(target);
    if (existingGroup.getFeatures().isEmpty()) {
      source.getChildren().remove(existingGroup);
    }

    // Remove the GEdge from the GModel
    modelState.getRoot().getChildren().remove(gEdge);
  }

  protected void createNewGroup(Feature source, Feature target, Group.GroupType groupType) {
    Group group = new Group(groupType);
    group.setParentFeature(source);
    group.getFeatures().add(target);
    source.getChildren().add(group);
  }

  protected Group.GroupType getGroupType(CreateEdgeOperation operation) {
    return convertModelTypeToGroupType(operation.getElementTypeId());
  }

  protected Feature getSourceFeature(CreateEdgeOperation operation) {
    return modelState
        .getIndex()
        .getUVLObject(operation.getSourceElementId(), Feature.class)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Source feature not found for ID: " + operation.getSourceElementId()));
  }

  protected Feature getTargetFeature(CreateEdgeOperation operation) {
    return modelState
        .getIndex()
        .getUVLObject(operation.getTargetElementId(), Feature.class)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Target feature not found for ID: " + operation.getTargetElementId()));
  }

  protected Optional<GEdge> findExistingEdge(CreateEdgeOperation operation) {
    return modelState.getIndex().getAllByClass(GEdge.class).stream()
        .filter(
            gEdge ->
                gEdge.getSourceId().equals(operation.getSourceElementId())
                    && gEdge.getTargetId().equals(operation.getTargetElementId()))
        .findFirst();
  }

  protected Optional<Group> findExistingGroup(Feature source, Group.GroupType groupType) {
    return source.getChildren().stream().filter(group -> group.GROUPTYPE == groupType).findFirst();
  }

  private boolean edgeAlreadyExists(CreateEdgeOperation operation, String uvlModelType) {
    Optional<GEdge> existingEdge = findExistingEdge(operation);
    return existingEdge.map(gEdge -> gEdge.getType().equals(uvlModelType)).orElse(false);
  }
}
