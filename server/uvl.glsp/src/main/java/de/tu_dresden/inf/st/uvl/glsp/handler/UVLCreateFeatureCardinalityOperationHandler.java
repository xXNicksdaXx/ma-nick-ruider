/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.glsp.utils.GModelUtil;
import de.tu_dresden.inf.st.uvl.metamodel.model.Cardinality;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.LanguageLevel;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.server.gmodel.GModelCreateOperationHandler;
import org.eclipse.glsp.server.operations.CreateNodeOperation;

public class UVLCreateFeatureCardinalityOperationHandler
    extends GModelCreateOperationHandler<CreateNodeOperation> {

  @Inject protected UVLModelState modelState;

  UVLCreateFeatureCardinalityOperationHandler() {
    super(UVLModelTypes.CARDINALITY_LABEL);
  }

  @Override
  public Optional<Command> createCommand(CreateNodeOperation operation) {
    return commandOf(() -> executeCreation(operation));
  }

  protected void executeCreation(CreateNodeOperation operation) {
    // find parent feature
    String containerId = operation.getContainerId();
    String featureId = Optional.ofNullable(GModelUtil.extractUUID(containerId)).orElse(containerId);
    Feature feature =
        modelState
            .getIndex()
            .getUVLObject(featureId, Feature.class)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No parent feature found for attribute with container ID: " + containerId));

    // add feature cardinality
    createFeatureCardinality(feature);

    // update the model index
    modelState.updateIndex();
  }

  protected void createFeatureCardinality(Feature feature) {
    // set default cardinality for the feature
    feature.setCardinality(new Cardinality(0));

    // add language level if not already present
    modelState.getFeatureModel().getUsedLanguageLevels().add(LanguageLevel.FEATURE_CARDINALITY);
  }
}
