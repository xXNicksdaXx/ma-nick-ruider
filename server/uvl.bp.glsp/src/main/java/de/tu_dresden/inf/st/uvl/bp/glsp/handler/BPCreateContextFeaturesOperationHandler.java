/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */

package de.tu_dresden.inf.st.uvl.bp.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.bp.glsp.BPModelTypes;
import de.tu_dresden.inf.st.uvl.bp.glsp.model.BPModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import java.util.Optional;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.server.gmodel.GModelCreateOperationHandler;
import org.eclipse.glsp.server.operations.CreateNodeOperation;

public class BPCreateContextFeaturesOperationHandler
    extends GModelCreateOperationHandler<CreateNodeOperation> {

  @Inject protected BPModelState modelState;

  BPCreateContextFeaturesOperationHandler() {
    super(BPModelTypes.BP_CONTEXT, BPModelTypes.BP_ENV, BPModelTypes.BP_CONFIG);
  }

  @Override
  public Optional<Command> createCommand(CreateNodeOperation operation) {
    return commandOf(this::executeCreation);
  }

  protected void executeCreation() {
    if (modelState.getFeatureModel().getEnv() == null) {
      Feature env = new Feature("Env");
      env.getAttributes().put("type", new Attribute<>("type", "Env", env));
      modelState.getFeatureModel().setEnv(env);
      modelState.getFeatureModel().getFeatureMap().put(env.getFeatureName(), env);
    }

    if (modelState.getFeatureModel().getConfig() == null) {
      Feature config = new Feature("Config");
      config.getAttributes().put("type", new Attribute<>("type", "Config", config));
      modelState.getFeatureModel().setConfig(config);
      modelState.getFeatureModel().getFeatureMap().put(config.getFeatureName(), config);
    }

    modelState.updateIndex();
  }
}
