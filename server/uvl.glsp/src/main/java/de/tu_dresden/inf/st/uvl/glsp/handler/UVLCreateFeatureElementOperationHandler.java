/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.handler;

import com.google.inject.Inject;
import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.vill.model.Attribute;
import de.vill.model.Feature;
import de.vill.model.LanguageLevel;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.server.gmodel.GModelCreateOperationHandler;
import org.eclipse.glsp.server.operations.CreateNodeOperation;

import java.util.Optional;

public class UVLCreateFeatureElementOperationHandler extends GModelCreateOperationHandler<CreateNodeOperation> {

    @Inject
    protected UVLModelState modelState;

    UVLCreateFeatureElementOperationHandler() {
        super(UVLModelTypes.ATTRIBUTE, UVLModelTypes.CARDINALITY_LABEL);
    }

    @Override
    public Optional<Command> createCommand(CreateNodeOperation operation) {
        return commandOf(() -> executeCreation(operation));
    }

    protected void executeCreation(CreateNodeOperation operation) {
        // find parent feature
        Feature feature = modelState.getIndex().getUVLObject(operation.getContainerId(), Feature.class).orElseThrow(() ->
                new IllegalStateException("No parent feature found for attribute with container ID: " + operation.getContainerId()));

        if (operation.getElementTypeId().equals(UVLModelTypes.ATTRIBUTE)) {
            createAttribute(feature);
        } else if (operation.getElementTypeId().equals(UVLModelTypes.CARDINALITY_LABEL)) {
            createFeatureCardinality(feature);
        }

        // update the model index
        modelState.updateIndex();
    }

    protected void createAttribute(Feature feature) {
        // create new Attribute and add it to the parent feature
        int attributeSize = feature.getAttributes().size();
        String name = "Attribute" + (attributeSize + 1);
        Attribute<Boolean> newAttribute = new Attribute<>(name, true);
        feature.getAttributes().put(name, newAttribute);
    }

    protected void createFeatureCardinality(Feature feature) {
        // set default cardinality for the feature
        feature.setLowerBound("0");
        feature.setUpperBound("1");

        // add language level if not already present
        modelState.getFeatureModel().getUsedLanguageLevels().add(LanguageLevel.FEATURE_CARDINALITY);
    }
}
