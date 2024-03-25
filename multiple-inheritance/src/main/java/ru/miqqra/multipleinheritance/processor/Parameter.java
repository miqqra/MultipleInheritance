package ru.miqqra.multipleinheritance.processor;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public record Parameter(
    String simpleName,
    TypeMirror type) {

    public Parameter(VariableElement variableElement) {
        this(variableElement.getSimpleName().toString(),
            variableElement.asType());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Parameter)) {
            return false;
        }
        return type == ((Parameter) o).type;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return type + " " + simpleName;
    }
}
