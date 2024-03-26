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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import ru.miqqra.multipleinheritance.MultipleInheritance;
import ru.miqqra.multipleinheritance.MultipleInheritanceObject;

@SupportedAnnotationTypes("ru.miqqra.multipleinheritance.MultipleInheritance")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

    private static final String CALL_NEXT_METHOD_PATTERN = "callNext%s";
    private static final String INTERMEDIARY_FIELD_PATTERN = "%sIntermediary";

    private AnnotatedClassParser classParser = null;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        if (classParser == null) {
            classParser = new AnnotatedClassParser(processingEnv);
        }
        Set<? extends Element> classes =
            roundEnv.getElementsAnnotatedWith(MultipleInheritance.class);
        for (var element : classes) {
            createImplementationFile((TypeElement) element);
        }
        return true;
    }

    private void createImplementationFile(TypeElement annotatedElement) {
        List<TypeElement> parents = classParser.get(annotatedElement).parents();
        ResolutionTableGenerator generator =
            new ResolutionTableGenerator(processingEnv, annotatedElement);
        List<TypeElement> resolutionTable = generator.getTable();

        TypeSpec.Builder implementationClass = TypeSpec.classBuilder(
                INTERMEDIARY_FIELD_PATTERN.formatted(annotatedElement.getSimpleName().toString()))
            .addModifiers(annotatedElement.getModifiers().toArray(new Modifier[0]))
            .superclass(MultipleInheritanceObject.class);
        implementationClass.addJavadoc("Parent classes: " +
            String.join(", ", parents.stream().map(TypeElement::toString).toList()));

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

        Set<Method> methods = classParser.get(annotatedElement).methods();

        methods.forEach(method -> {
            var methodSpec = createMethod(method, resolutionTable, fieldNames);
            var callNextMethodSpec =
                createCallNextMethod(method, resolutionTable, fieldNames);
            implementationClass.addMethod(methodSpec);
            implementationClass.addMethod(callNextMethodSpec);
        });

        TypeSpec implementation = implementationClass.build();
        String qualifiedName = annotatedElement.getQualifiedName().toString();
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

    private MethodSpec createMethod(Method method,
                                    List<TypeElement> resolutionTable,
                                    Map<TypeElement, String> fieldNames) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(method.simpleName())
            .addModifiers(method.element().getModifiers());
        method.element().getParameters()
            .forEach(v -> methodSpec.addParameter(ParameterSpec.get(v)));
        TypeName returnType = TypeName.get(method.returnType());
        methodSpec.returns(returnType);

        String callNextMethodName = CALL_NEXT_METHOD_PATTERN.formatted(method.simpleName());
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

    //    private MethodSpec createCallNextMethod(Map.Entry<String, ExecutableElement> nameAndMethodEntry,
    private MethodSpec createCallNextMethod(Method method,
                                            List<TypeElement> resolutionTable,
                                            Map<TypeElement, String> fieldNames) {
        String callNextMethodName = CALL_NEXT_METHOD_PATTERN.formatted(method.simpleName());

        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(callNextMethodName)
            .addModifiers(method.element().getModifiers());
        method.element().getParameters()
            .forEach(v -> methodSpec.addParameter(ParameterSpec.get(v)));

        TypeName returnType = TypeName.get(method.returnType());
        methodSpec.returns(returnType);

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder()
            .addStatement("currentNextMethod++");

        for (int i = 0; i < resolutionTable.size(); i++) {
            if (i == 0 && i == resolutionTable.size() - 1) {
                addStatements(
                    codeBlockBuilder.beginControlFlow(
                        "if (currentNextMethod == %d)".formatted(i + 1)),
                    method, resolutionTable, fieldNames, i).endControlFlow();
            } else if (i == 0) {
                addStatements(
                    codeBlockBuilder.beginControlFlow(
                        "if (currentNextMethod == %d)".formatted(i + 1)),
                    method, resolutionTable, fieldNames, i);
            } else if (i == resolutionTable.size() - 1) {
                addStatements(
                    codeBlockBuilder.nextControlFlow(
                        "if (currentNextMethod == %d)".formatted(i + 1)),
                    method, resolutionTable, fieldNames, i).endControlFlow();
            } else {
                addStatements(
                    codeBlockBuilder.nextControlFlow(
                        "if (currentNextMethod == %d)".formatted(i + 1)),
                    method, resolutionTable, fieldNames, i);
            }

        }
        return methodSpec.addCode(codeBlockBuilder.build()).build();
    }

    private CodeBlock.Builder addStatements(CodeBlock.Builder builder,
                                            Method method,
                                            List<TypeElement> resolutionTable,
                                            Map<TypeElement, String> fieldNames,
                                            int i) {
        String methodCallFormat = "$N.$N($L)";

        return builder

            .addStatement("$N.actualObject = this", fieldNames.get(resolutionTable.get(i)))
            .addStatement(methodCallFormat,
                fieldNames.get(resolutionTable.get(i)),
                method.simpleName(),
                CodeBlock.join(method.element()
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


}
