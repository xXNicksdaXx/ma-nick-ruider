package de.tu_dresden.inf.st.uvl.metamodel.model.building;

import de.tu_dresden.inf.st.uvl.metamodel.model.UVLObject;

/**
 * Classes that can be referenced in UVL constraints
 */
public interface VariableReference extends UVLObject {

    // TODO: Do we need more?
    // TODO: Add this to respective classes: Feature, Attribute, ??
    String getIdentifier();

}
