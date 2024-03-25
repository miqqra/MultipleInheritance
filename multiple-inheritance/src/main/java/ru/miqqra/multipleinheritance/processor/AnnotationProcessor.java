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
import java.util.Collection;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import ru.miqqra.multipleinheritance.MultipleInheritance;
import ru.miqqra.multipleinheritance.MultipleInheritanceObject;

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
        Set<? extends Element> classes =
            roundEnv.getElementsAnnotatedWith(MultipleInheritance.class);
        for (var element : classes) {
            createImplementationFile((TypeElement) element);
        }
        return true;
    }

    private void createImplementationFile(TypeElement inheritedClass) {
        List<TypeElement> parents = Util.getParents(inheritedClass, processingEnv);

//        ElementFilter.fieldsIn(List.of(parents.get(0)));
        ResolutionTableGenerator generator =
            new ResolutionTableGenerator(processingEnv, inheritedClass);
        List<TypeElement> resolutionTable = generator.getTable();

        TypeSpec.Builder implementationClass = TypeSpec.classBuilder(
                INTERMEDIARY_FIELD_PATTERN.formatted(inheritedClass.getSimpleName().toString()))
            .addModifiers(inheritedClass.getModifiers().toArray(new Modifier[0]))
            .superclass(MultipleInheritanceObject.class);
        implementationClass.addJavadoc("Parent classes: " +
            String.join(", ", parents.stream().map(TypeElement::toString).toList()));

        List<Parent> processedParents = new ArrayList<>();
        for (TypeElement parent : parents) {
            ClassMembers parentElements = membersFromClass(parent);
            processedParents.add(new Parent(parent, parentElements));
        }

//        if (!inheritedClass.getSimpleName().toString().equals("ResultClass")) {
//            return;
//        }
//        var q = new Method(ElementFilter.methodsIn(parents.get(0).getEnclosedElements()).get(0));
//        var w = new Method(ElementFilter.methodsIn(parents.get(1).getEnclosedElements()).get(0));
//        assert q.equals(w);

        for (int i = 0; i < resolutionTable.size(); i++) {
            TypeElement parent = resolutionTable.get(i);
            implementationClass.addField(createField(parent));
            // implementationClass.addField(createFieldIntermediary(parent));
//
//            var methodSpec = MethodSpec
//                        .methodBuilder(.getSimpleName().toString())
//                        .addModifiers(method.getModifiers());
        }
        Map<TypeElement, String> fieldNames = resolutionTable.stream()
            .collect(Collectors.toMap(v -> v, this::getParamName, (v1, v2) -> v2));

        Map<String, ExecutableElement> methods =
            processedParents.stream().map(Parent::classMembers).map(v -> v.methods)
                .flatMap(Collection::stream).collect(
                    Collectors.toMap(v -> v.getSimpleName().toString(), v -> v, (v1, v2) -> v2));
        methods.putAll(ElementFilter.methodsIn(inheritedClass.getEnclosedElements()).stream()
            .collect(Collectors.toMap(v -> v.getSimpleName().toString(), v -> v, (v1, v2) -> v2)));

        methods.entrySet().forEach(nameAndMethodEntry -> {
            var methodSpec = createMethod(nameAndMethodEntry, resolutionTable, fieldNames);
            var callNextMethodSpec =
                createCallNextMethod(nameAndMethodEntry, resolutionTable, fieldNames);
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
        return FieldSpec.builder(ClassName.get(parent.asType()), getParamName(parent))
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new " + parent.getSimpleName() + "()").build();
    }

    private MethodSpec createMethod(Map.Entry<String, ExecutableElement> nameAndMethodEntry,
                                    List<TypeElement> resolutionTable,
                                    Map<TypeElement, String> fieldNames) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(nameAndMethodEntry.getKey())
            .addModifiers(nameAndMethodEntry.getValue().getModifiers());
        nameAndMethodEntry.getValue().getParameters()
            .forEach(v -> methodSpec.addParameter(ParameterSpec.get(v)));
        TypeName returnType = TypeName.get(nameAndMethodEntry.getValue().getReturnType());
        methodSpec.returns(returnType);

        String callNextMethodName = CALL_NEXT_METHOD_PATTERN.formatted(nameAndMethodEntry.getKey());
        methodSpec.addCode(
            CodeBlock.builder()
                .beginControlFlow("if (actualObject != null)")
                .addStatement("var actual = actualObject")
                .addStatement("actualObject = null")
                .beginControlFlow("try")
                .addStatement("actual.getClass().getMethod(\"%s\").invoke(actual)"
                    .formatted(callNextMethodName))
                .nextControlFlow("catch (Exception e)")
                .addStatement("throw new RuntimeException(e)")
                .endControlFlow()
                .nextControlFlow("else")
                .addStatement("currentNextMethod = 0")
                .addStatement(callNextMethodName + "()")
                .endControlFlow()
                .build()
        );

//        String methodCallFormat;
//        if ("void".equals(returnType.toString())) {
//            methodCallFormat = "$N.$N($L)";
//        } else {
//            methodCallFormat = "$N.$N($L)"; //todo
//        }
//
//        resolutionTable.forEach(element -> methodSpec.addStatement(methodCallFormat, fieldNames.get(element), nameAndMethodEntry.getKey(), CodeBlock.join(nameAndMethodEntry.getValue().getParameters().stream().map(x -> CodeBlock.of(x.getSimpleName().toString())).toList(), ", ")));
//
//        String callNextMethodFormat = "$N.$N($L)";
//        resolutionTable.forEach(element -> methodSpec.addStatement(callNextMethodFormat, fieldNames.get(element), CALL_NEXT_METHOD_PATTERN.formatted(nameAndMethodEntry.getKey()), CodeBlock.join(nameAndMethodEntry.getValue().getParameters().stream().map(x -> CodeBlock.of(x.getSimpleName().toString())).toList(), ", ")));


        return methodSpec.build();
    }

    private MethodSpec createCallNextMethod(Map.Entry<String, ExecutableElement> nameAndMethodEntry,
                                            List<TypeElement> resolutionTable,
                                            Map<TypeElement, String> fieldNames) {
        String callNextMethodName = CALL_NEXT_METHOD_PATTERN.formatted(nameAndMethodEntry.getKey());

        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(callNextMethodName)
            .addModifiers(nameAndMethodEntry.getValue().getModifiers());
        nameAndMethodEntry.getValue().getParameters()
            .forEach(v -> methodSpec.addParameter(ParameterSpec.get(v)));

        TypeName returnType = TypeName.get(nameAndMethodEntry.getValue().getReturnType());
        methodSpec.returns(returnType);

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder()
            .addStatement("currentNextMethod++");

        for (int i = 0; i < resolutionTable.size(); i++) {
            if (i == 0 && i == resolutionTable.size() - 1) {
                addStatements(
                    codeBlockBuilder.beginControlFlow(
                        "if (currentNextMethod == %d)".formatted(i + 1)),
                    nameAndMethodEntry, resolutionTable, fieldNames, i).endControlFlow();
            } else if (i == 0) {
                addStatements(
                    codeBlockBuilder.beginControlFlow(
                        "if (currentNextMethod == %d)".formatted(i + 1)),
                    nameAndMethodEntry, resolutionTable, fieldNames, i);
            } else if (i == resolutionTable.size() - 1) {
                addStatements(
                    codeBlockBuilder.nextControlFlow(
                        "if (currentNextMethod == %d)".formatted(i + 1)),
                    nameAndMethodEntry, resolutionTable, fieldNames, i).endControlFlow();
            } else {
                addStatements(
                    codeBlockBuilder.nextControlFlow(
                        "if (currentNextMethod == %d)".formatted(i + 1)),
                    nameAndMethodEntry, resolutionTable, fieldNames, i);
            }

        }
        return methodSpec.addCode(codeBlockBuilder.build()).build();
    }

    private CodeBlock.Builder addStatements(CodeBlock.Builder builder,
                                            Map.Entry<String, ExecutableElement> nameAndMethodEntry,
                                            List<TypeElement> resolutionTable,
                                            Map<TypeElement, String> fieldNames,
                                            int i) {
        String methodCallFormat = "$N.$N($L)";

        return builder

            .addStatement("$N.actualObject = this", fieldNames.get(resolutionTable.get(i)))
            .addStatement(methodCallFormat,
                fieldNames.get(resolutionTable.get(i)),
                nameAndMethodEntry.getKey(),
                CodeBlock.join(nameAndMethodEntry.getValue()
                        .getParameters()
                        .stream()
                        .map(x -> CodeBlock.of(x.getSimpleName().toString()))
                        .toList(),
                    ", ")
            );
    }

    private String getParamName(TypeElement typeElement) {
        String className = typeElement.getSimpleName().toString();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    private String getParamName(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
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
