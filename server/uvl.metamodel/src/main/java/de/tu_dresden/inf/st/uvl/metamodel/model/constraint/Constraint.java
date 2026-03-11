package de.tu_dresden.inf.st.uvl.metamodel.model.constraint;

import de.tu_dresden.inf.st.uvl.metamodel.model.UVLObject;
import de.tu_dresden.inf.st.uvl.metamodel.model.building.VariableReference;

import java.util.List;

public abstract class Constraint implements UVLObject {
    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    private int lineNumber;

    @Override
    public String toString() {
        return toString(true, "");
    }

    public abstract String toString(boolean withSubmodels, String currentAlias);

    public abstract List<Constraint> getConstraintSubParts();

    public abstract void replaceConstraintSubPart(Constraint oldSubConstraint, Constraint newSubConstraint);

    @Override
    public abstract Constraint clone();

    @Override
    public int hashCode() {
        return hashCode(1);
    }

    public abstract int hashCode(int level);

    @Override
    public abstract boolean equals(Object obj);

    public abstract List<VariableReference> getReferences();
}

