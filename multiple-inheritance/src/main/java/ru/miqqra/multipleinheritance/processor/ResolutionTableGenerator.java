package ru.miqqra.multipleinheritance.processor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import ru.miqqra.multipleinheritance.MultipleInheritance;


public class ResolutionTableGenerator {

    private final ProcessingEnvironment processingEnv;

    public ResolutionTableGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public List<TypeElement> getTable(TypeElement inheritedClass) {
        List<TypeElement> table = new ArrayList<>();
        Map<TypeElement, Integer> visited = new HashMap<>();
        Deque<TypeElement> stack = new ArrayDeque<>();
        stack.push(inheritedClass);
        while (!stack.isEmpty()) {
            TypeElement current = stack.peek();
            if (!visited.containsKey(current)) {
                visited.put(current, 1);
                for (TypeElement parent : getParents(current)) {
                    if (!visited.containsKey(parent)) {
                        stack.push(parent);
                    } else if (visited.get(parent) == 1) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Inheritance graph contains cycle");
                        throw new RuntimeException("Inheritance graph contains cycle");
                    }
                }
            } else {
                if (visited.get(current) == 1) {
                    visited.put(current, 2);
                    table.add(current);
                }
                stack.pop();
            }
        }
        table.remove(table.size() - 1);
        Collections.reverse(table);
        return table;
    }

    private List<TypeElement> getParents(TypeElement inheritedClass) {
        List<TypeElement> parents;
        try {
            //noinspection ResultOfMethodCallIgnored
            inheritedClass.getAnnotation(MultipleInheritance.class).classes();
            return List.of();
        } catch (MirroredTypesException mte) {
            return mte.getTypeMirrors().stream()
                .map(x -> (TypeElement) mirrorToElement(x))
                .toList();
        }
    }

    private Element mirrorToElement(TypeMirror x) {
        return processingEnv.getTypeUtils().asElement(x);
    }
}
