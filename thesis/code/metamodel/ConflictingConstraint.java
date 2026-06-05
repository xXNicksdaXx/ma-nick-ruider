public class ConflictingConstraint extends Constraint {
    private final List<VariableReference> references;

    public ConflictingConstraint(List<VariableReference> references) { ... }

    public List<VariableReference> getReferenceList() {
        return Collections.unmodifiableList(references);
    }
    public void setReference(int index, VariableReference reference) {
        this.references.set(index, reference);
    }
    @Override
    public String toString(boolean withSubmodels, String currentAlias) { ... }
}