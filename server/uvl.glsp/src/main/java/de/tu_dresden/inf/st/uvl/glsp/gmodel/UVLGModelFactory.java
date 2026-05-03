/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.gmodel;

import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil.getAllGroups;
import static de.tu_dresden.inf.st.uvl.glsp.utils.FeatureModelUtil.getEdgeConstraints;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.FeatureModel;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;
import org.eclipse.glsp.server.features.core.model.GModelFactory;
import org.eclipse.glsp.server.utils.ClientOptionsUtil;

public class UVLGModelFactory implements GModelFactory {
  protected static Logger LOGGER = LogManager.getLogger(UVLGModelFactory.class.getSimpleName());

  @Inject protected UVLModelState modelState;

  @Inject protected UVLFeatureFactory featureFactory;

  @Inject protected UVLGroupFactory groupFactory;

  @Inject protected UVLBiConstraintFactory biConstraintFactory;

  @Inject protected UVLConstraintBoxFactory constraintBoxFactory;

  @Override
  public void createGModel() {
    FeatureModel featureModel = modelState.getFeatureModel();
    GGraph newRoot = createRootElement();

    fillRootElement(newRoot, featureModel);
    modelState.updateRoot(newRoot);
  }

  protected GGraph createRootElement() {
    return new GGraphBuilder(DefaultTypes.GRAPH)
        .id(ClientOptionsUtil.getSourceUri(modelState.getClientOptions()).orElse("root"))
        .revision(modelState.getRoot() != null ? modelState.getRoot().getRevision() : -1)
        .build();
  }

  protected void fillRootElement(GGraph root, FeatureModel featureModel) {
    featureModel.getFeatureMap().values().stream()
        .map(featureFactory::create)
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
  }
}
