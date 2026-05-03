/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.gmodel;

import static de.tu_dresden.inf.st.uvl.bp.glsp.utils.BTypeUtil.isConfigFeature;
import static de.tu_dresden.inf.st.uvl.bp.glsp.utils.BTypeUtil.isEnvFeature;
import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil.*;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.gmodel.UVLGModelFactory;
import de.tu_dresden.inf.st.uvl.metamodel.model.BPFeatureModel;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import java.util.Collection;
import org.eclipse.glsp.graph.GGraph;

public class BPGModelFactory extends UVLGModelFactory {

  @Inject protected BPFeatureFactory bpFeatureFactory;

  @Inject protected BPAdditionalElementsFactory additionalElementsFactory;

  protected void fillRootElement(GGraph root, FeatureModel featureModel) {
    featureModel.getFeatureMap().values().stream()
        .filter(feature -> !isConfigFeature(feature) && !isEnvFeature(feature))
        .map(bpFeatureFactory::create)
        .forEachOrdered(root.getChildren()::add);

    getAllGroups(featureModel).stream()
        .map(groupFactory::create)
        .flatMap(Collection::stream)
        .forEachOrdered(root.getChildren()::add);

    getEdgeConstraints(featureModel).stream()
        .map(biConstraintFactory::create)
        .forEachOrdered(root.getChildren()::add);

    if (featureModel.getRootFeature() != null) {
      root.getChildren().add(constraintBoxFactory.create(featureModel));
    }

    if (featureModel instanceof BPFeatureModel bpFeatureModel) {
      root.getChildren().addAll(additionalElementsFactory.createAdditionalElements(bpFeatureModel));
    }
  }
}
