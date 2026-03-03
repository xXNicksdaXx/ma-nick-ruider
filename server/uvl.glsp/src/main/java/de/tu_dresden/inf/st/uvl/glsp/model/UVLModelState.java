/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.model;

import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import org.eclipse.glsp.server.model.GModelState;

public interface UVLModelState extends GModelState {
    FeatureModel getFeatureModel();

    void setFeatureModel(FeatureModel model);

    void updateIndex();

    @Override
    UVLModelIndex getIndex();
}
