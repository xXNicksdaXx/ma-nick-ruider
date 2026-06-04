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

public class BPCreateBThreadOperationHandler extends UVLCreateFeatureOperationHandler {

  BPCreateBThreadOperationHandler() {
    super(BPModelTypes.B_THREAD);
  }

  @Override
  protected String getCreatedNodeType() {
    return BPModelTypes.B_THREAD;
  }

  @Override
  protected Feature createFeature(final Feature parentFeature) {
    Feature bThread = super.createFeature(parentFeature);
    bThread.getAttributes().put("type", new Attribute<>("type", "BThread", bThread));
    return bThread;
  }

  @Override
  protected String getFeatureName() {
    int currentFeatureCount = modelState.getFeatureModel().getFeatureMap().size();
    return "BThread" + (currentFeatureCount + 1);
  }
}
