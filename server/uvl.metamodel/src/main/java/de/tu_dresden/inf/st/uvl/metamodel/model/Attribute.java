package de.tu_dresden.inf.st.uvl.metamodel.model;

import de.tu_dresden.inf.st.uvl.metamodel.model.building.VariableReference;
import de.tu_dresden.inf.st.uvl.metamodel.model.constraint.Constraint;
import de.tu_dresden.inf.st.uvl.metamodel.util.Constants;

import javax.management.AttributeList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class represents an Attribute.
 * There is an separated class and not just Objects in the attributes map to be able to reference a single attribute
 * for example in a constraint.
 *
 * @param <T> The type of the value
 */
public class Attribute<T> implements VariableReference {

    private int line;
    private String name;
    private T value;
    private Feature feature;

    /**
     * The constructor of the attribute class takes an attribute name (does not contain the feature name) and a value of type T
     *
     * @param name                 the name of the attribute (must be different from all other attributes of the feature)
     * @param value                the value of the attribute
     * @param correspondingFeature the feature this attribute belongs to
     */
    public Attribute(String name, T value, Feature correspondingFeature) {
        this.name = Objects.requireNonNull(name);
        this.value = Objects.requireNonNull(value);
        this.feature = Objects.requireNonNull(correspondingFeature);
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    /**
     * Returns the value of the attribute.
     *
     * @return Value of the attribute (never null)
     */
    public T getValue() {
        return value;
    }

    public void setValue(T value) { this.value = value; }

    /**
     * Returns the name of the attribute.
     *
     * @return Name of the attribute (never null)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }


    /**
     * Returns the type of the attribute
     * @return Name of the attribute (never null)
     */
    public String getType() {
        return switch (value) {
            case Boolean ignored -> Constants.BOOLEAN;
            case String ignored -> Constants.STRING;
            case AttributeList ignored -> Constants.ATTRIBUTE_LIST;
            case Number ignored -> Constants.NUMBER;
            case null, default -> Constants.UNDEF;
        };
    }

    /**
     * Returns the feature this attribute is attached to
     * @return Feature
     */
    public Feature getFeature() {return feature;}

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    /**
     * Returns a uvl representation of the attribute as string (different for the possible types of the value)
     *
     * @return attribute as string
     */
    public String toString(boolean withSubmodels, String currentAlias) {
    	//should never be the case but who knows...
    	if (value == null) {
    		return "";
    	}
        StringBuilder result = new StringBuilder();
        switch (value) {
            case Map map -> {
                //attributes map to string
                result.append("{");
                if (!map.isEmpty()) {
                    map.forEach((k, v) -> {
                        result.append(k);
                        result.append(' ');
                        if (v instanceof Attribute) {
                            result.append(((Attribute<?>) v).toString(withSubmodels, currentAlias));
                        } else {
                            result.append(v);
                        }
                        result.append(',');
                        result.append(' ');
                    });
                    //remove comma after last entry
                    result.setLength(result.length() - 2);
                }
                result.append("}");
            }
            case List list -> {
                //vector (list) of attributes to string
                result.append("[");
                if (!list.isEmpty()) {
                    for (Object item : list) {
                        if (item instanceof Constraint) {
                            result.append(((Constraint) item).toString(withSubmodels, currentAlias));
                        } else {
                            result.append(item);
                        }
                        result.append(", ");
                    }
                    result.setLength(result.length() - 2);
                }
                result.append("]");
            }
            case String s -> {
                result.append("'");
                result.append(s);
                result.append("'");
            }
            case Constraint constraint -> result.append(constraint.toString(withSubmodels, currentAlias));
            default -> result.append(value);
        }
        return result.toString();
    }
    
    @Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

    @Override
    public boolean equals(Object obj) {
    	if (this == obj) {
    		return true;
    	}
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Attribute<?> other = (Attribute<?>) obj;
        return Objects.equals(name, other.name)
        		&& Objects.equals(value, other.value);
    }

    @Override
    public String getIdentifier() {
        return feature.getIdentifier() + "." + name;
    }

    @Override
    public Attribute<T> clone(){
        return new Attribute<>(name, value, feature);
    }

    public Attribute<T> cloneWithFeature(Feature feature){
        return new Attribute<>(name, value, feature);
    }
}
