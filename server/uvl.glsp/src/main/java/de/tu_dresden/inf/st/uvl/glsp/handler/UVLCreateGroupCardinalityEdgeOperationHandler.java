/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.handler;

import de.tu_dresden.inf.st.uvl.glsp.UVLModelTypes;
import de.tu_dresden.inf.st.uvl.metamodel.model.Cardinality;
import de.tu_dresden.inf.st.uvl.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.metamodel.model.Group;
import de.tu_dresden.inf.st.uvl.metamodel.model.LanguageLevel;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.server.operations.CreateEdgeOperation;

import java.util.Optional;

public class UVLCreateGroupCardinalityEdgeOperationHandler  extends UVLCreateRelationEdgeOperationHandler {

    UVLCreateGroupCardinalityEdgeOperationHandler() {
        super(UVLModelTypes.GROUP_CARDINALITY);
    }

    @Override
    protected void executeCreation(CreateEdgeOperation operation) {
        addGroupCardinalityLanguageLevel();

        Feature sourceFeature = getSourceFeature(operation);
        Feature targetFeature = getTargetFeature(operation);

        // check for an existing edge between the two features (must be replaced)
        Optional<GEdge> existingEdge = findExistingEdge(operation);
        existingEdge.ifPresent(gEdge -> removeExistingEdge(sourceFeature, targetFeature, gEdge));

        // create or update group for mandatory relationship
        Optional<Group> existingGroup = findExistingGroup(sourceFeature, Group.GroupType.GROUP_CARDINALITY);
        existingGroup.ifPresentOrElse(
                // update: add target feature to existing group
                group -> group.getFeatures().add(targetFeature),
                // create new group and add target feature
                () -> createGroupWithCardinality(sourceFeature, targetFeature)
        );

        // update the model index
        modelState.updateIndex();
    }

    protected void addGroupCardinalityLanguageLevel() {
        modelState.getFeatureModel().getUsedLanguageLevels().add(LanguageLevel.GROUP_CARDINALITY);
    }

    protected void createGroupWithCardinality(Feature source, Feature target) {
        Group group = new Group(Group.GroupType.GROUP_CARDINALITY);
        group.setCardinality(new Cardinality(0));
        group.setParentFeature(source);
        group.getFeatures().add(target);
        source.getChildren().add(group);
    }
}
