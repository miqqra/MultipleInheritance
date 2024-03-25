package ru.miqqra.multipleinheritance.processor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import ru.miqqra.multipleinheritance.MultipleInheritance;


public class ResolutionTableGenerator {

    private final ProcessingEnvironment processingEnv;
    private final TypeElement inheritedClass;

    public ResolutionTableGenerator(ProcessingEnvironment processingEnv,
                                    TypeElement inheritedClass) {
        this.processingEnv = processingEnv;
        this.inheritedClass = inheritedClass;
    }

    public List<TypeElement> getTable() {
        List<TypeElement> table = new ArrayList<>();
        Map<TypeElement, Integer> visited = new HashMap<>();
        Deque<TypeElement> stack = new ArrayDeque<>();
        stack.push(inheritedClass);
        while (!stack.isEmpty()) {
            TypeElement current = stack.peek();
            if (!visited.containsKey(current)) {
                visited.put(current, 1);
                for (TypeElement parent : Util.getParents(current, processingEnv)) {
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
}
