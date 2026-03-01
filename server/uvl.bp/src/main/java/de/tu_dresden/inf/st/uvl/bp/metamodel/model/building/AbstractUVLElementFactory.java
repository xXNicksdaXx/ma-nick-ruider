package de.tu_dresden.inf.st.uvl.bp.metamodel.model.building;

import de.tu_dresden.inf.st.uvl.bp.metamodel.model.Attribute;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.Feature;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.FeatureModel;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.GlobalAttribute;
import de.tu_dresden.inf.st.uvl.bp.metamodel.exception.ParseError;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.*;
import de.tu_dresden.inf.st.uvl.bp.metamodel.model.constraint.LiteralConstraint;

public abstract class AbstractUVLElementFactory {

    public abstract Feature createFeature(String name);

    public abstract <T> Attribute<T> createAttribute(String name, T value, Feature correspondingFeature);

    public abstract GlobalAttribute createGlobalAttribute(String identifier, FeatureModel featureModel);

}