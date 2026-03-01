package de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression;

public abstract class BinaryExpression extends Expression{

    public abstract Expression getLeft();
    public abstract Expression getRight();

}
