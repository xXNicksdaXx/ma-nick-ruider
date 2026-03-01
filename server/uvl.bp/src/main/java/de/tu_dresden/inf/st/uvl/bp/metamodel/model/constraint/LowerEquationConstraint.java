package de.tu_dresden.inf.st.uvl.bp.metamodel.model.constraint;

import de.tu_dresden.inf.st.uvl.bp.metamodel.model.expression.Expression;
import de.tu_dresden.inf.st.uvl.bp.metamodel.util.ConstantSymbols;

import java.util.Collections;
import java.util.List;

public class LowerEquationConstraint extends ExpressionConstraint {
    public LowerEquationConstraint(final Expression left, final Expression right) {
        super(left, right, ConstantSymbols.LOWER);
    }

    @Override
    public List<Constraint> getConstraintSubParts() {
        return Collections.emptyList();
    }

    @Override
    public Constraint clone() {
        return new LowerEquationConstraint(getLeft().clone(), getRight().clone());
    }
}
