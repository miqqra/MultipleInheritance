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
import java.lang.reflect.Array;
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
import javax.lang.model.type.TypeMirror;
import ru.miqqra.multipleinheritance.MultipleInheritance;

@SupportedAnnotationTypes("ru.miqqra.multipleinheritance.MultipleInheritance")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

    private static final String CALL_NEXT_METHOD_PATTERN = "callNext%s";
    private static final String INTERMEDIARY_FIELD_PATTERN = "%sIntermediary";

    private static final String INIT_PARENTS_VARIABLE_NAME = "_initParents";
    private static final String ACTUAL_OBJECT_VARIABLE_NAME = "_actualObject";
    private static final String CURRENT_NEXT_METHOD_VARIABLE_NAME = "_currentNextMethod";

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
        List<TypeElement> resolutionTable = classParser.get(annotatedElement).resolutionTable();

        TypeSpec.Builder implementationClass = TypeSpec.classBuilder(
                        INTERMEDIARY_FIELD_PATTERN.formatted(annotatedElement.getSimpleName().toString()))
                .addModifiers(annotatedElement.getModifiers().toArray(new Modifier[0]));
        for (var i: classParser.get(annotatedElement).interfaces()) {
            implementationClass.addSuperinterface(i.mirror());
        }
        implementationClass.addJavadoc("Parent classes: " +
                String.join(", ", parents.stream().map(TypeElement::toString).toList()));

        createDefaultFields().forEach(implementationClass::addField);

        for (TypeElement parent : resolutionTable) {
            implementationClass.addField(createField(parent));
            // implementationClass.addField(createFieldIntermediary(parent));
//
//            var methodSpec = MethodSpec
//                        .methodBuilder(.getSimpleName().toString())
//                        .addModifiers(method.getModifiers());
        }

        Map<TypeElement, String> fieldNames = resolutionTable.stream()
                .collect(Collectors.toMap(v -> v, this::getParamName, (v1, v2) -> v2));

        implementationClass.addMethod(createConstructor(resolutionTable, fieldNames));

        Set<Method> methods = classParser.get(annotatedElement).methods();

        methods.forEach(method -> {
            var methodSpec = createMethod(method);
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

    private MethodSpec createConstructor(List<TypeElement> resolutionTable,
                                         Map<TypeElement, String> fieldNames) {
        var constructorBuilder = MethodSpec.constructorBuilder();

        var codeBlockBuilder = CodeBlock.builder()
                .beginControlFlow("if (%s == null)".formatted(INIT_PARENTS_VARIABLE_NAME));

        for (TypeElement parent : resolutionTable) {
            codeBlockBuilder
                    .addStatement("%s.%s = new Object[] {%s}"
                            .formatted(
                                    parent.getSimpleName(),
                                    INIT_PARENTS_VARIABLE_NAME,
                                    classParser.get(parent)
                                            .resolutionTable()
                                            .stream()
                                            .map(fieldNames::get)
                                            .collect(Collectors.joining(", "))
                            ))
                    .addStatement("%s = new %s()"
                            .formatted(
                                    fieldNames.get(parent),
                                    parent.getSimpleName()
                            ));
        }

        codeBlockBuilder.nextControlFlow("else");

        for (int i = 0; i < resolutionTable.size(); i++) {
            codeBlockBuilder.addStatement("%s = (%s) %s[%d]"
                    .formatted(
                            fieldNames.get(resolutionTable.get(i)),
                            resolutionTable.get(i).getSimpleName(),
                            INIT_PARENTS_VARIABLE_NAME,
                            i));
        }

        codeBlockBuilder.addStatement("%s = null".formatted(INIT_PARENTS_VARIABLE_NAME));
        codeBlockBuilder.endControlFlow();

        return constructorBuilder.addCode(codeBlockBuilder.build()).build();
    }

    private List<FieldSpec> createDefaultFields() {
        return List.of(
                FieldSpec.builder(Object[].class, INIT_PARENTS_VARIABLE_NAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .initializer("null").build(),
                FieldSpec.builder(TypeName.OBJECT, ACTUAL_OBJECT_VARIABLE_NAME)
                        .addModifiers(Modifier.PUBLIC)
                        .build(),
                FieldSpec.builder(TypeName.INT, CURRENT_NEXT_METHOD_VARIABLE_NAME)
                        .addModifiers(Modifier.PRIVATE)
                        .initializer("0").build()
        );
    }

    private FieldSpec createField(TypeElement parent) {
        return FieldSpec.builder(ClassName.get(parent.asType()), getParamName(parent))
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    private MethodSpec createMethod(Method method) {

        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(method.simpleName())
                .addModifiers(method.element().getModifiers());
        method.element().getParameters()
                .forEach(v -> methodSpec.addParameter(ParameterSpec.get(v)));
        TypeName returnType = TypeName.get(method.returnType());
        methodSpec.returns(returnType);

        String callNextMethodName = CALL_NEXT_METHOD_PATTERN.formatted(method.simpleName());
        CodeBlock parameters = codeBlockParameters(method);
        CodeBlock parametersTypes = codeBlockParameterTypes(method);

        methodSpec.addCode(
                CodeBlock.builder()
                        .beginControlFlow("if (%s != null)".formatted(ACTUAL_OBJECT_VARIABLE_NAME))
                        .addStatement("var actual = %s".formatted(ACTUAL_OBJECT_VARIABLE_NAME))
                        .addStatement("%s = null".formatted(ACTUAL_OBJECT_VARIABLE_NAME))
                        .beginControlFlow("try")
                        .addStatement(returnType.equals(TypeName.VOID)
                                ? createMethodGetPositiveResultVoid(parameters, parametersTypes, callNextMethodName)
                                : createMethodGetPositiveResultWithReturn(parameters, parametersTypes, callNextMethodName, method))
                        .nextControlFlow("catch (Exception e)")
                        .addStatement("throw new RuntimeException(e)")
                        .endControlFlow()
                        .nextControlFlow("else")
                        .addStatement("%s = 0".formatted(CURRENT_NEXT_METHOD_VARIABLE_NAME))
                        .addStatement(
                                returnType.equals(TypeName.VOID) ? "$N($L)" : "return $N($L)",
                                callNextMethodName,
                                parameters)
                        .endControlFlow()
                        .build()
        );
        return methodSpec.build();
    }

    private CodeBlock createMethodGetPositiveResultVoid(CodeBlock parameters,
                                                        CodeBlock parametersTypes,
                                                        String callNextMethodName) {
        String invokeReflectionVoid = "actual.getClass().getMethod(\"$N\"$L$L).invoke(actual$L$L)";

        return CodeBlock.of(invokeReflectionVoid,
                callNextMethodName,
                (parameters.isEmpty() ? "" : ", "),
                parametersTypes,
                (parameters.isEmpty() ? "" : ", "),
                parameters);
    }

    private CodeBlock createMethodGetPositiveResultWithReturn(CodeBlock parameters,
                                                              CodeBlock parametersTypes,
                                                              String callNextMethodName,
                                                              Method method) {
        String invokeReflectionWithReturn = "return ($L) actual.getClass().getMethod(\"$N\"$L$L).invoke(actual$L$L)";

        return CodeBlock.of(invokeReflectionWithReturn,
                method.returnType().toString(),
                callNextMethodName,
                (parameters.isEmpty() ? "" : ", "),
                parametersTypes,
                (parameters.isEmpty() ? "" : ", "),
                parameters);
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
                .addStatement("%s++".formatted(CURRENT_NEXT_METHOD_VARIABLE_NAME));

        List<TypeElement> methodTable = resolutionTable.stream()
            .filter(x -> classParser.get(x).methods().contains(method))
            .toList();

        for (int i = methodTable.size() - 1; i >= 0; i--) {
            if (i == 0 && i == methodTable.size() - 1) {
                addStatements(
                        codeBlockBuilder.beginControlFlow("if (%s == %d)"
                                .formatted(CURRENT_NEXT_METHOD_VARIABLE_NAME, methodTable.size() - i)),
                        method, returnType, methodTable, fieldNames, i)
                        .endControlFlow();
            } else if (i == methodTable.size() - 1) {
                addStatements(
                        codeBlockBuilder.beginControlFlow("if (%s == %d)"
                                .formatted(CURRENT_NEXT_METHOD_VARIABLE_NAME, methodTable.size() - i)),
                        method, returnType, methodTable, fieldNames, i);
            } else if (i == 0) {
                addStatements(
                        codeBlockBuilder.nextControlFlow("else if (%s == %d)"
                                .formatted(CURRENT_NEXT_METHOD_VARIABLE_NAME, methodTable.size() - i)),
                        method, returnType, methodTable, fieldNames, i).endControlFlow();
            } else {
                addStatements(
                        codeBlockBuilder.nextControlFlow("else if (%s == %d)"
                                .formatted(CURRENT_NEXT_METHOD_VARIABLE_NAME, methodTable.size() - i)),
                        method, returnType, methodTable, fieldNames, i);
            }
        }

        if (!returnType.equals(TypeName.VOID)) {
            codeBlockBuilder.addStatement("return $L",
                    returnType.isPrimitive()
                            ? PrimitiveTypesUtil.PRIMITIVE_TYPES_DEFAULTS.get(returnType)
                            : Array.get(Array.newInstance(method.returnType().getClass(), 1), 0));
        }
        return methodSpec.addCode(codeBlockBuilder.build()).build();
    }

    private CodeBlock.Builder addStatements(CodeBlock.Builder builder,
                                            Method method,
                                            TypeName returnType,
                                            List<TypeElement> resolutionTable,
                                            Map<TypeElement, String> fieldNames,
                                            int i) {
        return builder
                .addStatement("$N.%s = this".formatted(ACTUAL_OBJECT_VARIABLE_NAME),
                        fieldNames.get(resolutionTable.get(i)))
                .addStatement(
                        returnType.equals(TypeName.VOID) ? "$N.$N($L)" : "return $N.$N($L)",
                        fieldNames.get(resolutionTable.get(i)),
                        method.simpleName(),
                        codeBlockParameters(method)
                );
    }

    private static CodeBlock codeBlockParameters(Method method) {
        return CodeBlock.join(method.element()
                        .getParameters()
                        .stream()
                        .map(x -> CodeBlock.of(x.getSimpleName().toString()))
                        .toList(),
                ", ");
    }

    private static CodeBlock codeBlockParameterTypes(Method method) {
        return CodeBlock.join(method.parameters()
                        .stream()
                        .map(Parameter::type)
                        .map(TypeMirror::toString)
                        .map(v -> v + ".class")
                        .map(CodeBlock::of)
                        .toList(),
                ",");
    }

    private String getParamName(TypeElement typeElement) {
        String className = typeElement.getSimpleName().toString();
        return "__" + Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

}
