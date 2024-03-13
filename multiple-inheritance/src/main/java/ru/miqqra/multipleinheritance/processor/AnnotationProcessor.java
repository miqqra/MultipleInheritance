package ru.miqqra.multipleinheritance.processor;

import static javax.lang.model.element.Modifier.PUBLIC;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Map;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypesException;
import ru.miqqra.multipleinheritance.annotations.Inheritance;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("ru.miqqra.multipleinheritance.annotations.Inheritance")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        Set<? extends Element> classes = roundEnv.getElementsAnnotatedWith(Inheritance.class);
        for (var element : classes) {
            createImplementationFile((TypeElement) element);
        }
        return true;
    }

    private Class<?>[] getValue(Map<? extends ExecutableElement,
        ? extends AnnotationValue> values,
                                String name) {
        for (Map.Entry<? extends ExecutableElement,
            ? extends AnnotationValue> e : values.entrySet()) {
            if (name.contentEquals(e.getKey().getSimpleName())) {
                return (Class<?>[]) e.getValue().getValue();
            }
        }
        return null;
    }

    private void createImplementationFile(TypeElement inheritedClass) {
        List<TypeElement> parents;
        try {
            //noinspection ResultOfMethodCallIgnored
            inheritedClass.getAnnotation(Inheritance.class).classes();
            return;
        } catch (MirroredTypesException mte) {
            parents = mte.getTypeMirrors().stream()
                .map(x -> (TypeElement) processingEnv.getTypeUtils().asElement(x)).toList();
        }

//        ElementFilter.fieldsIn(List.of(parents.get(0)));


        TypeSpec.Builder implementationClass = TypeSpec.classBuilder(
                inheritedClass.getSimpleName().toString() + "Impl")
            .addModifiers(inheritedClass.getModifiers().toArray(new Modifier[0]))
//            .superclass((TypeName) inheritedClass)
            .superclass(TypeName.get(inheritedClass.asType()));
        implementationClass.addJavadoc("Parent classes: " + String.join(", ",
            parents.stream().map(TypeElement::toString).toList()));


//        //todo
//        Arrays.stream(inheritedClass.getClass().getMethods()).filter(v -> v.isAnnotationPresent(Inherit.class));
//
//        List<FieldSpec> fields1 = Arrays.stream(inheritedClass
//                .getAnnotation(Inheritance.class)
//                .classes())
//            .map(v -> FieldSpec.builder(v, v.getSimpleName())
//                .addModifiers(PRIVATE)
//                .build())
//            .toList();
//
//        fields.forEach(implementationClass::addField);

        for (Element methodElement : inheritedClass.getEnclosedElements()) {
            if (methodElement.getKind() == ElementKind.METHOD) {
                MethodSpec.Builder methodBuilder =
                    MethodSpec.overriding((ExecutableElement) methodElement)
                        .addModifiers(PUBLIC)
                        .addCode(CodeBlock.of("System.out.println(\"hello\");"));
                // TODO: Add method body according to your requirements
                // You can use methodBuilder.addStatement("your code here");

                implementationClass.addMethod(methodBuilder.build());
            }
        }

        TypeSpec implementation = implementationClass.build();
        String qualifiedName = inheritedClass.getQualifiedName().toString();
        String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
        JavaFile javaFile =
            JavaFile.builder(packageName, implementation).build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
