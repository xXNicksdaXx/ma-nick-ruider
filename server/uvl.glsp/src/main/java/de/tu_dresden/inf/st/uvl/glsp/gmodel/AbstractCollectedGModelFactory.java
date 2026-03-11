/*
 * Copyright © 2026 Nick Ruider. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 */
package de.tu_dresden.inf.st.uvl.glsp.gmodel;

import de.tu_dresden.inf.st.uvl.glsp.model.UVLModelState;
import de.tu_dresden.inf.st.uvl.metamodel.model.UVLObject;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GModelElement;

import com.google.inject.Inject;
import org.eclipse.glsp.graph.builder.impl.GEdgeBuilder;

import java.util.Collection;
import java.util.Optional;

public abstract class AbstractCollectedGModelFactory<E extends UVLObject, T extends GModelElement> {

    @Inject
    protected UVLModelState modelState;

    protected abstract Collection<T> create(E object);

    protected void applyEdgeData(GEdgeBuilder edgeBuilder, String id) {
        Optional<GEdge> existingEdge = modelState.getIndex().getGModelElement(id, GEdge.class);
        existingEdge.ifPresent(edge -> edgeBuilder
                .addRoutingPoints(edge.getRoutingPoints()));
    }
}
