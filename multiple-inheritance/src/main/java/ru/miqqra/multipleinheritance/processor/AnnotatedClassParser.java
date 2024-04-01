package ru.miqqra.multipleinheritance.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

public class AnnotatedClassParser {
    private final ProcessingEnvironment processingEnv;
    private final Map<TypeElement, AnnotatedClass> classMap = new HashMap<>();

    public AnnotatedClassParser(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        Method.processingEnv = processingEnv;
        Parameter.processingEnv = processingEnv;
    }

    public AnnotatedClass get(TypeElement annotatedElement) {
        if (classMap.containsKey(annotatedElement)) {
            return classMap.get(annotatedElement);
        } else {
            var ret = parseClass(annotatedElement);
            classMap.put(annotatedElement, ret);
            return ret;
        }
    }

    private AnnotatedClass parseClass(TypeElement annotatedElement) {
        List<TypeElement> parents = Util.getParents(annotatedElement, processingEnv);
        List<ExecutableElement> declaredMethodsList =
            ElementFilter.methodsIn(annotatedElement.getEnclosedElements());
        Set<Method> declaredMethods =
            declaredMethodsList.stream().map(Method::new).collect(Collectors.toSet());
        Set<Method> methods = new HashSet<>(declaredMethods);
        for (TypeElement parent : parents) {
            methods.addAll(get(parent).methods());
        }
        var generator = new ResolutionTableGenerator(processingEnv, annotatedElement);
        return new AnnotatedClass(parents, generator.getTable(), declaredMethodsList,
            declaredMethods, methods);
    }
}
