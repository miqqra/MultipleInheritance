package ru.miqqra.multipleinheritance.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import ru.miqqra.multipleinheritance.annotations.Inheritance;

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
            parents =
                mte.getTypeMirrors().stream().map(x -> (TypeElement) mirrorToElement(x)).toList();
        }

//        ElementFilter.fieldsIn(List.of(parents.get(0)));


        TypeSpec.Builder implementationClass = TypeSpec.classBuilder(
                inheritedClass.getSimpleName().toString() + "Impl")
            .addModifiers(inheritedClass.getModifiers().toArray(new Modifier[0]))
//            .superclass((TypeName) inheritedClass)
            .superclass(TypeName.get(inheritedClass.asType()));
        implementationClass.addJavadoc("Parent classes: " + String.join(", ",
            parents.stream().map(TypeElement::toString).toList()));

        TypeElement commonParent = (TypeElement) mirrorToElement(inheritedClass.getSuperclass());
        ClassMembers commonMembers = membersFromClass(commonParent, null);
        ClassMembers newMembers = membersFromClass(inheritedClass, commonParent);
        {
            // for now
            var newMethods = newMembers.methods.stream()
                .filter(x -> !x.getModifiers().contains(Modifier.ABSTRACT)).toList();
            newMembers.methods.clear();
            newMembers.methods.addAll(newMethods);
        }

        List<Parent> processedParents = new ArrayList<>();
        for (TypeElement parent : parents) {
            ClassMembers parentElements = membersFromClass(parent, commonParent);
            processedParents.add(new Parent(parent, parentElements));
        }

        for (int i = 0; i < processedParents.size(); i++) {
            Parent parent = processedParents.get(i);
            implementationClass.addField(
                FieldSpec.builder(ClassName.get(parent.element.asType()), "__parent" + i)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .initializer("new " + parent.element.getSimpleName() + "()")
                    .build()
            );
        }

        Set<String> alreadyKnownMethods =
            commonMembers.methods.stream().map(Object::toString).collect(Collectors.toSet());
        Set<String> ambiguousMethods = new HashSet<>();
        for (Parent parent : processedParents) {
            for (ExecutableElement method : parent.classMembers.methods) {
                if (method.getKind() != ElementKind.METHOD) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Should be method, but it's not: " + method);
                }
                if (alreadyKnownMethods.contains(method.toString())) {
                    ambiguousMethods.add(method.toString());
                } else {
                    alreadyKnownMethods.add(method.toString());
                }
            }
        }
        Set<String> newMethods =
            newMembers.methods.stream().map(Object::toString).collect(Collectors.toSet());
        for (int i = 0; i < processedParents.size(); i++) {
            Parent parent = processedParents.get(i);
            for (ExecutableElement method : parent.classMembers.methods) {
                if (!ambiguousMethods.contains(method.toString()) &&
                    !newMethods.contains(method.toString())) {
                    var methodSpec = MethodSpec
//                        .overriding(method)
                        .methodBuilder(method.getSimpleName().toString())
                        .addModifiers(method.getModifiers());
                    for (var parameter : method.getParameters()) {
                        methodSpec.addParameter(ParameterSpec.get(parameter));
                    }
                    TypeName returnType = TypeName.get(method.getReturnType());
                    methodSpec.returns(returnType);
                    String format;
                    if ("void".equals(returnType.toString())) {
                        format = "$N.$N($L)";
                    } else {
                        format = "return $N.$N($L)";
                    }
                    methodSpec.addStatement(format,
                        "__parent" + i,
                        method.getSimpleName().toString(),
                        CodeBlock.join(method.getParameters().stream()
                            .map(x -> CodeBlock.of(x.getSimpleName().toString())).toList(), ", ")
                    );
                    implementationClass.addMethod(methodSpec.build());
                }
            }
        }

//        //todo
//        Arrays.stream(inheritedClass.getClass().getMethods()).filter(v -> v.isAnnotationPresent(Inherit.class));
//
//        List<FieldSpec> fields1 = Arrays.stream(inheritedClass
//                .getAnnotation(Inheritance.class)
//                .classes())
//            .map(v -> FieldSpec.builder((Type) v, v.getSimpleName())
//                .addModifiers(Modifier.PRIVATE)
//                .build())
//            .toList();
//
//        fields.forEach(implementationClass::addField);

//        for (Element methodElement : inheritedClass.getEnclosedElements()) {
//            if (methodElement.getKind() == ElementKind.METHOD) {
//                MethodSpec.Builder methodBuilder =
//                    MethodSpec.overriding((ExecutableElement) methodElement)
//                        .addModifiers(Modifier.PUBLIC)
//                        .addCode(CodeBlock.of("System.out.println(\"hello\");"));
//                // TODO: Add method body according to your requirements
//                // You can use methodBuilder.addStatement("your code here");
//
//                implementationClass.addMethod(methodBuilder.build());
//            }
//        }

        TypeSpec implementation = implementationClass.build();
        String qualifiedName = inheritedClass.getQualifiedName().toString();
        String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
        JavaFile javaFile = JavaFile
            .builder(packageName, implementation)
            .indent("    ")
            .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Element mirrorToElement(TypeMirror x) {
        return processingEnv.getTypeUtils().asElement(x);
    }

    private static class ClassMembers {
        public Set<VariableElement> fields;
        public Set<ExecutableElement> methods;
    }


    /**
     * Get elements that were declared in the class or its parents until some parent met.
     */
    private ClassMembers membersFromClass(TypeElement from, TypeElement base) {
        ClassMembers ce = new ClassMembers();
        ce.fields = new HashSet<>(ElementFilter.fieldsIn(from.getEnclosedElements()));
        ce.methods = new HashSet<>(ElementFilter.methodsIn(from.getEnclosedElements()));
        TypeElement superclass = (TypeElement) mirrorToElement(from.getSuperclass());
        if (superclass != base && superclass != null) {
            ClassMembers ce1 = membersFromClass(superclass, base);
            ce.fields.addAll(ce1.fields);
            ce.methods.addAll(ce1.methods);
        }
        return ce;
    }

    private record Parent(TypeElement element, ClassMembers classMembers) {
    }
}
