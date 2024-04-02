package ru.miqqra.multipleinheritance.processor;

import java.util.List;
import java.util.Objects;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

public record Method(
    String simpleName,
    TypeMirror returnType,
    List<Parameter> parameters,
    ExecutableElement element) {
    public static ProcessingEnvironment processingEnv;

    public Method(ExecutableElement executableElement) {
        this(executableElement.getSimpleName().toString(),
            executableElement.getReturnType(),
            executableElement.getParameters().stream().map(Parameter::new).toList(),
            executableElement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Method method = (Method) o;
        return Objects.equals(simpleName, method.simpleName) &&
            processingEnv.getTypeUtils().isSameType(returnType, method.returnType) &&
            Objects.equals(parameters, method.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simpleName, returnType.toString(), parameters);
    }
}
