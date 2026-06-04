/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import java.util.List;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.GPoint;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.builder.impl.GNodeBuilder;
import org.eclipse.glsp.graph.util.GraphUtil;
import org.eclipse.glsp.server.actions.SelectAction;
import org.eclipse.glsp.server.gmodel.GModelCreateOperationHandler;
import org.eclipse.glsp.server.operations.CreateNodeOperation;

public class UVLCreateFeatureOperationHandler
    extends GModelCreateOperationHandler<CreateNodeOperation> {

  @Inject protected UVLModelState modelState;

  protected UVLCreateFeatureOperationHandler(final String... elementTypeIds) {
    super(elementTypeIds);
  }

  UVLCreateFeatureOperationHandler() {
    this(UVLModelTypes.FEATURE);
  }

  protected String getCreatedNodeType() {
    return UVLModelTypes.FEATURE;
  }

  @Override
  public Optional<Command> createCommand(CreateNodeOperation operation) {
    return commandOf(() -> executeCreation(operation));
  }

  protected void executeCreation(CreateNodeOperation operation) {
    if (isMissingRootFeature()) {
      createRootFeature(operation);
      return;
    }

    // get parent feature and its corresponding GNode
    Feature parentFeature = getInitialFeatureLink(operation.getContainerId());
    GNode parentNode =
        modelState
            .getIndex()
            .getGModelElement(parentFeature, GNode.class)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No GNode found for parent feature: " + parentFeature.getFeatureName()));

    // create and add new Feature to the model
    Feature feature = createFeature(parentFeature);

    // update the model index
    modelState.updateIndex();

    // create new GNode based on parent position
    GPoint location = calculateNewFeatureLocation(parentNode.getPosition());
    GNode featureNode = createGNode(location, feature);
    modelState.getRoot().getChildren().add(featureNode);

    // select the new feature node
    selectElement(featureNode);
  }

  protected void createRootFeature(CreateNodeOperation operation) {
    // create root Feature
    String featureName = getFeatureName();
    Feature rootFeature = new Feature(featureName);

    // add root feature to the model and set it as the root feature
    FeatureModel featureModel = modelState.getFeatureModel();
    featureModel.getFeatureMap().put(featureName, rootFeature);
    featureModel.setRootFeature(rootFeature);
    modelState.setFeatureModel(featureModel);

    // update the model index
    modelState.updateIndex();
  }

  protected Feature getInitialFeatureLink(String containerId) {
    Optional<Feature> parentFeature =
        modelState.getIndex().getUVLObject(containerId, Feature.class);
    // if no parent feature found, return the root feature
    return parentFeature.orElseGet(
        () -> {
          String rootId =
              modelState
                  .getIndex()
                  .getIdFor(modelState.getFeatureModel().getRootFeature())
                  .orElseThrow(
                      () -> new IllegalStateException("Root feature not indexed in the model"));
          return modelState
              .getIndex()
              .getUVLObject(rootId, Feature.class)
              .orElseThrow(() -> new IllegalStateException("Root feature not found in the model"));
        });
  }

  protected GNode createGNode(GPoint location, Feature feature) {
    String id =
        modelState
            .getIndex()
            .getIdFor(feature)
            .orElseThrow(() -> new IllegalStateException("Feature not indexed in the model"));

    // generate a new GNode with a label containing the feature name
    GNodeBuilder nodeBuilder =
        new GNodeBuilder(getCreatedNodeType())
            .id(id)
            .add(
                new GLabelBuilder(DefaultTypes.LABEL)
                    .id(id + "_label")
                    .text(getFeatureName())
                    .build())
            .size(64, 32);

    // set position based on the provided location or default to (0, 0)
    if (location != null) {
      nodeBuilder.position(location.getX(), location.getY());
    } else {
      nodeBuilder.position(0, 0);
    }

    return nodeBuilder.build();
  }

  protected Feature createFeature(Feature parentFeature) {
    // create new Feature
    Feature feature = new Feature(getFeatureName());

    List<Group> children = parentFeature.getChildren();
    if (children.isEmpty()) {
      // create new optional group, linking parent feature and new feature
      Group newGroup = new Group(Group.GroupType.OPTIONAL);
      newGroup.setParentFeature(parentFeature);
      newGroup.getFeatures().add(feature);
      parentFeature.getChildren().add(newGroup);
    } else {
      // add new feature to the first existing group
      Group firstGroup = children.getFirst();
      firstGroup.getFeatures().add(feature);
    }

    modelState.getFeatureModel().getFeatureMap().put(feature.getFeatureName(), feature);
    return feature;
  }

  protected String getFeatureName() {
    int currentFeatureCount = modelState.getFeatureModel().getFeatureMap().size();
    return "Feature" + (currentFeatureCount + 1);
  }

  protected GPoint calculateNewFeatureLocation(GPoint parentPosition) {
    final double verticalSpacing = 96.0;
    double randomOffset = Math.random() * 256 - 128;
    double x = parentPosition.getX() + randomOffset;
    double y = parentPosition.getY() + verticalSpacing;
    return GraphUtil.point(x, y);
  }

  public boolean isMissingRootFeature() {
    FeatureModel featureModel = modelState.getFeatureModel();
    boolean isEmpty = featureModel.getFeatureMap().isEmpty();
    boolean noRoot = featureModel.getRootFeature() == null;
    return isEmpty && noRoot;
  }

  protected void selectElement(GNode node) {
    actionDispatcher.dispatchAfterNextUpdate(SelectAction.setSelection(List.of(node.getId())));
  }
}
