package ru.miqqra.multipleinheritance.processor;

import java.util.Objects;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public record Parameter(
    String simpleName,
    TypeMirror type) {

    public static ProcessingEnvironment processingEnv;

    public Parameter(VariableElement variableElement) {
        this(variableElement.getSimpleName().toString(),
            variableElement.asType());
    }

    @Override
    public String toString() {
        return type + " " + simpleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Parameter parameter = (Parameter) o;
        return processingEnv.getTypeUtils().isSameType(type, parameter.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type.toString());
    }
}
