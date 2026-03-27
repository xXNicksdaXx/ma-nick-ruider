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
import de.tu_dresden.inf.st.uvl.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.metamodel.model.Cardinality;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.LanguageLevel;
import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.server.gmodel.GModelCreateOperationHandler;
import org.eclipse.glsp.server.operations.CreateNodeOperation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UVLCreateAttributeOperationHandler extends GModelCreateOperationHandler<CreateNodeOperation> {

    @Inject
    protected UVLModelState modelState;

    UVLCreateAttributeOperationHandler() {
        super(UVLModelTypes.ATTRIBUTE, UVLModelTypes.CARDINALITY_LABEL);
    }

    @Override
    public Optional<Command> createCommand(CreateNodeOperation operation) {
        return commandOf(() -> executeCreation(operation));
    }

    protected void executeCreation(CreateNodeOperation operation) {
        // find parent feature
        String containerId = operation.getContainerId();
        String featureId = Optional.ofNullable(GModelUtil.extractUUID(containerId)).orElse(containerId);
        Feature feature = modelState.getIndex().getUVLObject(featureId, Feature.class).orElseThrow(() ->
                new IllegalStateException("No parent feature found for attribute with container ID: " + containerId));

        // create attribute
        createAttribute(feature, containerId);

        // update the model index
        modelState.updateIndex();
    }

    protected void createAttribute(Feature feature, String containerId) {
        Map<String, Attribute<?>> targetAttributes = resolveTargetAttributes(feature, containerId);
        String name = generateAttributeName(targetAttributes);
        Attribute<Boolean> newAttribute = new Attribute<>(name, true, feature);
        targetAttributes.put(name, newAttribute);
    }

    protected Map<String, Attribute<?>> resolveTargetAttributes(final Feature feature, final String containerId) {
        List<String> path = GModelUtil.extractAttributePath(containerId);
        if (path.isEmpty()) {
            return feature.getAttributes();
        }

        GModelUtil.ResolvedAttribute resolvedAttribute = GModelUtil.resolveAttribute(feature, path)
                .orElseThrow(() -> new IllegalArgumentException("Could not resolve target attribute from container ID: " + containerId));

        return GModelUtil.asAttributeMap(resolvedAttribute.attribute())
                .orElseThrow(() -> new IllegalArgumentException("Target attribute does not support sub attributes: " + resolvedAttribute.attribute().getName()));
    }

    protected String generateAttributeName(final Map<String, Attribute<?>> attributes) {
        int index = attributes.size() + 1;
        String name = "Attribute" + index;
        while (attributes.containsKey(name)) {
            index++;
            name = "Attribute" + index;
        }
        return name;
    }
}
