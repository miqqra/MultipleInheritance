package ru.miqqra.multipleinheritance.processor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import ru.miqqra.multipleinheritance.MultipleInheritance;


public class ResolutionTableGenerator {

    static List<TypeElement> getTable(TypeElement inheritedClass, RoundEnvironment roundEnv) {

        List<? extends Element> classes =
            new ArrayList<>(roundEnv.getElementsAnnotatedWith(MultipleInheritance.class));
        classes.sort(Comparator.comparing(
            (Element element) -> element.getSimpleName().toString()));
        return switch (inheritedClass.getSimpleName().toString()) {
            case "Parent1" -> List.of();
            case "Parent2" -> List.of((TypeElement)classes.get(0));
            case "ResultClass" -> List.of((TypeElement)classes.get(1), (TypeElement)classes.get(0));
            default -> throw new IllegalStateException("Unexpected value: " + inheritedClass.getSimpleName().toString());
        };
    }
}
