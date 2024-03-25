package ru.miqqra.multipleinheritance.processor;

import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import ru.miqqra.multipleinheritance.MultipleInheritance;

public class Util {
    public static Element mirrorToElement(TypeMirror x, ProcessingEnvironment processingEnv) {
        return processingEnv.getTypeUtils().asElement(x);
    }

    public static List<TypeElement> getParents(TypeElement inheritedClass, ProcessingEnvironment processingEnv) {
        List<TypeElement> parents;
        try {
            //noinspection ResultOfMethodCallIgnored
            inheritedClass.getAnnotation(MultipleInheritance.class).classes();
            return List.of();
        } catch (MirroredTypesException mte) {
            return mte.getTypeMirrors().stream()
                .map(x -> (TypeElement) Util.mirrorToElement(x, processingEnv))
                .toList();
        }
    }
}
