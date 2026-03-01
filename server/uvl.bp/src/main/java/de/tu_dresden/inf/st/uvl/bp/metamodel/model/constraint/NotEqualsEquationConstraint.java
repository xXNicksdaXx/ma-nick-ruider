package de.tu_dresden.inf.st.uvl.bp.metamodel.model.constraint;

import de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression.Expression;
import de.tu_dresden.inf.st.uvl.bp.metamodel.util.ConstantSymbols;

import java.util.Collections;
import java.util.List;

public class NotEqualsEquationConstraint extends ExpressionConstraint {
    public NotEqualsEquationConstraint(final Expression left, final Expression right) {
        super(left, right, ConstantSymbols.NOT_EQUALS);
    }

    @Override
    public List<Constraint> getConstraintSubParts() {
        return Collections.emptyList();
    }

    @Override
    public Constraint clone() {
        return new NotEqualsEquationConstraint(getLeft().clone(), getRight().clone());
    }
}
