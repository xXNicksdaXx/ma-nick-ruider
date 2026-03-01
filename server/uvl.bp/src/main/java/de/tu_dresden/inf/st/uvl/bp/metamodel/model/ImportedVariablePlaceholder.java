package de.tu_dresden.inf.st.uvl.bp.metamodel.model;

import de.tu_dresden.inf.st.uvl.bp.metamodel.model.building.VariableReference;

import java.util.List;

public class ImportedVariablePlaceholder implements VariableReference {

    public final Import mainImport;
    public final List<String> unidentifiedImportParts;

    public ImportedVariablePlaceholder(Import relatedImport, List<String> unidentifiedImportParts) {
        this.mainImport = relatedImport;
        this.unidentifiedImportParts = unidentifiedImportParts;
    }

    @Override
    public String getIdentifier() {
        return String.format("%s.%s", mainImport.getAlias(), String.join(".", unidentifiedImportParts));
    }
}
