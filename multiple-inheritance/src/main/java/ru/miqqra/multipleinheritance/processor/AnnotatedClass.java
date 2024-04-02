package ru.miqqra.multipleinheritance.processor;

import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public record AnnotatedClass(
    List<TypeElement> parents,
    List<TypeElement> resolutionTable,
    List<ExecutableElement> declaredMethodsList, // if you need Element or their order
    Set<Method> declaredMethods,
    Set<Method> methods,
    Set<Type> interfaces) {
}
