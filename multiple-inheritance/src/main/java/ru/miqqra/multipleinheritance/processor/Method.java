package ru.miqqra.multipleinheritance.processor;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public record Method(
    String simpleName,
    TypeMirror returnType,
    List<Parameter> parameters) {
    public Method(ExecutableElement executableElement) {
        this(executableElement.getSimpleName().toString(),
            executableElement.getReturnType(),
            executableElement.getParameters().stream().map(Parameter::new).toList());
    }
}
