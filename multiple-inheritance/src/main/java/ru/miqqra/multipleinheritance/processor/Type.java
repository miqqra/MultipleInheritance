package ru.miqqra.multipleinheritance.processor;

import java.util.Objects;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

public record Type(TypeMirror mirror) {
    public static ProcessingEnvironment processingEnv;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Type type = (Type) o;
        return processingEnv.getTypeUtils().isSameType(mirror, type.mirror);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mirror.toString());
    }
}
