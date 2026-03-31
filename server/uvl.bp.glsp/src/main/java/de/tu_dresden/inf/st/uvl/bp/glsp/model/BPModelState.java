/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.model;

import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.BPFeatureModel;

public interface BPModelState extends UVLModelState {
  @Override
  BPFeatureModel getFeatureModel();

  void setFeatureModel(BPFeatureModel model);
}
