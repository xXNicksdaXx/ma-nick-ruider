/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.bp.glsp.model.BPModelState;
import de.tu_dresden.inf.st.uvl.glsp.handler.UVLDeleteOperationHandler;
import de.tu_dresden.inf.st.uvl.metamodel.model.UVLObject;

public class BPDeleteOperationHandler extends UVLDeleteOperationHandler {

  @Inject protected BPModelState modelState;

  @Override
  protected void deleteUvlObject(final UVLObject uvlObject) {
    // remove from context fields
    if (modelState.getFeatureModel().getEnv() == uvlObject) {
      modelState.getFeatureModel().setEnv(null);
    } else if (modelState.getFeatureModel().getConfig() == uvlObject) {
      modelState.getFeatureModel().setConfig(null);
    }

    super.deleteUvlObject(uvlObject);
  }

  @Override
  protected boolean isDeletableAttributeElement(final String elementType) {
    return super.isDeletableAttributeElement(elementType)
        || BPModelTypes.REQUESTED_EVENT.equals(elementType)
        || BPModelTypes.BLOCKED_EVENT.equals(elementType)
        || BPModelTypes.WAITED_FOR_EVENT.equals(elementType);
  }
}
