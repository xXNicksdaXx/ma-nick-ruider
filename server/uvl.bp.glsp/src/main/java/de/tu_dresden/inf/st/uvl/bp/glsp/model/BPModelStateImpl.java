/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.model;

import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelStateImpl;
import de.tu_dresden.inf.st.uvl.metamodel.model.BPFeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;

public class BPModelStateImpl extends UVLModelStateImpl implements BPModelState {

  private BPFeatureModel bpFeatureModel;

  @Override
  public BPFeatureModel getFeatureModel() {
    return this.bpFeatureModel;
  }

  @Override
  public void setFeatureModel(FeatureModel model) {
    if (model instanceof BPFeatureModel bpModel) {
      setFeatureModel(bpModel);
    } else {
      throw new IllegalArgumentException(
          "Expected a BPFeatureModel, but got: " + model.getClass().getSimpleName());
    }
  }

  @Override
  public void setFeatureModel(BPFeatureModel model) {
    this.bpFeatureModel = model;
  }
}
