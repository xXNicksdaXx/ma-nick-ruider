/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.handler;

import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLCreateFeatureOperationHandler;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import java.util.List;

public class BPCreateBThreadOperationHandler extends UVLCreateFeatureOperationHandler {

  BPCreateBThreadOperationHandler() {
    super(BPModelTypes.B_THREAD);
  }

  @Override
  protected String getCreatedNodeType() {
    return BPModelTypes.B_THREAD;
  }

  @Override
  protected void createFeature(final Feature parentFeature) {
    // create new BThread feature and mark it via type attribute
    Feature bThread = new Feature(getFeatureName());
    bThread.getAttributes().put("type", new Attribute<>("type", "BThread", bThread));

    List<Group> children = parentFeature.getChildren();
    if (children.isEmpty()) {
      Group newGroup = new Group(Group.GroupType.OPTIONAL);
      newGroup.setParentFeature(parentFeature);
      newGroup.getFeatures().add(bThread);
      parentFeature.getChildren().add(newGroup);
    } else {
      Group firstGroup = children.getFirst();
      firstGroup.getFeatures().add(bThread);
    }

    modelState.getFeatureModel().getFeatureMap().put(bThread.getFeatureName(), bThread);
  }

  @Override
  protected String getFeatureName() {
    int currentFeatureCount = modelState.getFeatureModel().getFeatureMap().size();
    return "BThread" + (currentFeatureCount + 1);
  }
}
