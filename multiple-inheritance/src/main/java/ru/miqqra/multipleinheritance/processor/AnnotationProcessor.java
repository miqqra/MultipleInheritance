package ru.miqqra.multipleinheritance.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import ru.miqqra.multipleinheritance.MultipleInheritance;
import ru.miqqra.multipleinheritance.MultipleInheritanceObject;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("ru.miqqra.multipleinheritance.MultipleInheritance")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

    private static final String CALL_NEXT_METHOD_PATTERN = "callNext%s";
    private static final String INTERMEDIARY_FIELD_PATTERN = "%sIntermediary";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        Set<? extends Element> classes = roundEnv.getElementsAnnotatedWith(MultipleInheritance.class);
        for (var element : classes) {
            createImplementationFile((TypeElement) element, roundEnv);
        }
        return true;
    }

    private void createImplementationFile(TypeElement inheritedClass, RoundEnvironment roundEnv) {
        List<TypeElement> parents;
        try {
            //noinspection ResultOfMethodCallIgnored
            inheritedClass.getAnnotation(MultipleInheritance.class).classes();
            return;
        } catch (MirroredTypesException mte) {
            parents = mte.getTypeMirrors().stream().map(x -> (TypeElement) mirrorToElement(x)).toList();
        }

//        ElementFilter.fieldsIn(List.of(parents.get(0)));
        List<TypeElement> resolutionTable = ResolutionTableGenerator.getTable(inheritedClass, roundEnv);


        TypeSpec.Builder implementationClass = TypeSpec.classBuilder(INTERMEDIARY_FIELD_PATTERN.formatted(inheritedClass.getSimpleName().toString())).addModifiers(inheritedClass.getModifiers().toArray(new Modifier[0])).superclass(MultipleInheritanceObject.class);
        implementationClass.addJavadoc("Parent classes: " + String.join(", ", parents.stream().map(TypeElement::toString).toList()));

        List<Parent> processedParents = new ArrayList<>();
        for (TypeElement parent : parents) {
            ClassMembers parentElements = membersFromClass(parent);
            processedParents.add(new Parent(parent, parentElements));
        }

        for (int i = 0; i < resolutionTable.size(); i++) {
            TypeElement parent = resolutionTable.get(i);
            implementationClass.addField(createField(parent));
            // implementationClass.addField(createFieldIntermediary(parent));
//
//            var methodSpec = MethodSpec
//                        .methodBuilder(.getSimpleName().toString())
//                        .addModifiers(method.getModifiers());
        }
        Map<TypeElement, String> fieldNames = resolutionTable.stream().collect(Collectors.toMap(v -> v, this::getParamName, (v1, v2) -> v2));

        Map<String, ExecutableElement> methods = processedParents.stream().map(Parent::classMembers).map(v -> v.methods).flatMap(Collection::stream).collect(Collectors.toMap(v -> v.getSimpleName().toString(), v -> v, (v1, v2) -> v2));

        methods.entrySet().forEach(nameAndMethodEntry -> {
            var methodSpec = createMethod(nameAndMethodEntry, resolutionTable, fieldNames);
            var callNextMethodSpec = createCallNextMethod(nameAndMethodEntry, resolutionTable, fieldNames);
            implementationClass.addMethod(methodSpec);
            implementationClass.addMethod(callNextMethodSpec);
        });


//        for (int i = 0; i < processedParents.size(); i++) {
//            Parent parent = processedParents.get(i);
//            for (ExecutableElement method : parent.classMembers.methods) {
//                if (true) {
//                    var methodSpec = MethodSpec
////                        .overriding(method)
//                        .methodBuilder(method.getSimpleName().toString())
//                        .addModifiers(method.getModifiers());
//                    for (var parameter : method.getParameters()) {
//                        methodSpec.addParameter(ParameterSpec.get(parameter));
//                    }
//                    TypeName returnType = TypeName.get(method.getReturnType());
//                    methodSpec.returns(returnType);
//                    String format;
//                    if ("void".equals(returnType.toString())) {
//                        format = "$N.$N($L)";
//                    } else {
//                        format = "return $N.$N($L)";
//                    }
//                    methodSpec.addStatement(format,
//                        "__parent" + i,
//                        method.getSimpleName().toString(),
//                        CodeBlock.join(method.getParameters().stream()
//                            .map(x -> CodeBlock.of(x.getSimpleName().toString())).toList(), ", ")
//                    );
//                    implementationClass.addMethod(methodSpec.build());
//                }
//            }
//        }

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
        JavaFile javaFile = JavaFile.builder(packageName, implementation).indent("    ").build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private FieldSpec createField(TypeElement parent) {
        return FieldSpec.builder(ClassName.get(parent.asType()), getParamName(parent)).addModifiers(Modifier.PRIVATE, Modifier.FINAL).initializer("new " + parent.getSimpleName() + "()").build();
    }

    private MethodSpec createMethod(Map.Entry<String, ExecutableElement> nameAndMethodEntry, List<TypeElement> resolutionTable, Map<TypeElement, String> fieldNames) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(nameAndMethodEntry.getKey()).addModifiers(nameAndMethodEntry.getValue().getModifiers());
        nameAndMethodEntry.getValue().getParameters().forEach(v -> methodSpec.addParameter(ParameterSpec.get(v)));
        TypeName returnType = TypeName.get(nameAndMethodEntry.getValue().getReturnType());
        methodSpec.returns(returnType);

        String methodCallFormat;
        if ("void".equals(returnType.toString())) {
            methodCallFormat = "$N.$N($L)";
        } else {
            methodCallFormat = "$N.$N($L)"; //todo
        }

        resolutionTable.forEach(element -> methodSpec.addStatement(methodCallFormat, fieldNames.get(element), nameAndMethodEntry.getKey(), CodeBlock.join(nameAndMethodEntry.getValue().getParameters().stream().map(x -> CodeBlock.of(x.getSimpleName().toString())).toList(), ", ")));

        String callNextMethodFormat = "$N.$N($L)";
        resolutionTable.forEach(element -> methodSpec.addStatement(callNextMethodFormat, fieldNames.get(element), CALL_NEXT_METHOD_PATTERN.formatted(nameAndMethodEntry.getKey()), CodeBlock.join(nameAndMethodEntry.getValue().getParameters().stream().map(x -> CodeBlock.of(x.getSimpleName().toString())).toList(), ", ")));


        return methodSpec.build();
    }

    private MethodSpec createCallNextMethod(Map.Entry<String, ExecutableElement> entry, List<TypeElement> resolutionTable, Map<TypeElement, String> fieldNames) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(CALL_NEXT_METHOD_PATTERN.formatted(entry.getKey())).addModifiers(entry.getValue().getModifiers());
        entry.getValue().getParameters().forEach(v -> methodSpec.addParameter(ParameterSpec.get(v)));

        return methodSpec.build();
    }

    private String getParamName(TypeElement typeElement) {
        String className = typeElement.getSimpleName().toString();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    private String getParamName(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
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
    private ClassMembers membersFromClass(TypeElement from) {
        ClassMembers ce = new ClassMembers();
        ce.fields = new HashSet<>(ElementFilter.fieldsIn(from.getEnclosedElements()));
        ce.methods = new HashSet<>(ElementFilter.methodsIn(from.getEnclosedElements()));
//        TypeElement superclass = (TypeElement) mirrorToElement(from.getSuperclass());
//        if (superclass != base && superclass != null) {
//            ClassMembers ce1 = membersFromClass(superclass, base);
//            ce.fields.addAll(ce1.fields);
//            ce.methods.addAll(ce1.methods);
//        }
        return ce;
    }

    private record Parent(TypeElement element, ClassMembers classMembers) {
    }
}
