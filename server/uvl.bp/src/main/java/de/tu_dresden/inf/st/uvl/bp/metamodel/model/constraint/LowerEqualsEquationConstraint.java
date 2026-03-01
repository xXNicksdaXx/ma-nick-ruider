package de.tu_dresden.inf.st.uvl.bp.metamodel.model.constraint;

import de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression.Expression;
import de.tu_dresden.inf.st.uvl.bp.metamodel.util.ConstantSymbols;

import java.util.Collections;
import java.util.List;

public class LowerEqualsEquationConstraint extends ExpressionConstraint {
    public LowerEqualsEquationConstraint(final Expression left, final Expression right) {
        super(left, right, ConstantSymbols.LOWER_OR_EQUAL);
    }

    @Override
    public List<Constraint> getConstraintSubParts() {
        return Collections.emptyList();
    }

    @Override
    public Constraint clone() {
        return new LowerEqualsEquationConstraint(getLeft().clone(), getRight().clone());
    }
}
