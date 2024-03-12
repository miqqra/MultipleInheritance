package ru.miqqra.multipleinheritance.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import static javax.lang.model.element.Modifier.PUBLIC;
import ru.miqqra.multipleinheritance.annotations.Inheritance;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
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
        Elements elements = processingEnv.getElementUtils();
        Set<? extends Element> classes = roundEnv.getElementsAnnotatedWith(Inheritance.class);
        for (var element : classes) {
            String packageName = "org.example";
            String className = element.getSimpleName().toString();

            TypeSpec.Builder implementationClass = TypeSpec.classBuilder(className + "Impl")
                    .addModifiers(PUBLIC)
                    .superclass(TypeName.get(element.asType()));

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "AAAAAAAAAAA");
            //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, element.asType().toString());
            //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, element.getAnnotationMirrors().stream().map(Objects::toString).reduce((a, b) -> a + " " + b).orElse(null));

            for (var mirror : element.getAnnotationMirrors()) {
                DeclaredType annotationType = mirror.getAnnotationType();
                Element annotationDecl = annotationType.asElement();

//                try {
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, element.asType().toString());
//                    Class<?> a = Class.forName(element.asType().toString());
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, a.getSimpleName());
////                    var ann = a.getAnnotation(Inheritance.class);
////                    ann.classes();
////                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, ann.classes().toString());
//                } catch (ClassNotFoundException e) {
//                    throw new RuntimeException(e);
//                }

//                Map<? extends ExecutableElement, ? extends AnnotationValue> values =
//                        elements.getElementValuesWithDefaults(mirror);
//
//                var classes1 = (Class<?>[]) getValue(values, "classes");
//
//                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, classes1.toString());
//
//                for (var clazz : classes1) {
//                    var field = FieldSpec.builder(clazz.getClass(), " ").addModifiers(PRIVATE).build();
//                }

//                List<FieldSpec> fields = classes1.stream()
//                        .map(v ->
//                                //FieldSpec.builder(String.class, "aa")
//                                //.addModifiers(PRIVATE)
//                                //        .build()
//                        )
//                        .toList();

                //fields.forEach(implementationClass::addField);
            }


            for (Element methodElement : element.getEnclosedElements()) {
                if (methodElement.getKind() == ElementKind.METHOD) {
                    MethodSpec.Builder methodBuilder = MethodSpec.overriding((ExecutableElement) methodElement)
                            .addModifiers(PUBLIC)
                            .addCode(CodeBlock.of("System.out.println(\"hello\");"));
                    // TODO: Add method body according to your requirements
                    // You can use methodBuilder.addStatement("your code here");

                    implementationClass.addMethod(methodBuilder.build());
                }
            }

            TypeSpec implementation = implementationClass.build();
            JavaFile javaFile = JavaFile.builder(packageName, implementation).build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private Class<?>[] getValue(Map<? extends ExecutableElement,
            ? extends AnnotationValue> values,
                                String name) {
        for (Map.Entry<? extends ExecutableElement,
                ? extends AnnotationValue> e : values.entrySet()) {
            if (name.contentEquals(e.getKey().getSimpleName()))
                return (Class<?>[]) e.getValue().getValue();
        }
        return null;
    }

    private void createImplementationFile(TypeElement typeElement, Element element) {
        String packageName = "org.example";
        String className = typeElement.getSimpleName().toString();

        TypeSpec.Builder implementationClass = TypeSpec.classBuilder(className + "Impl")
                .addModifiers(PUBLIC)
                .superclass(TypeName.get(typeElement.asType()));

//        //todo
//        Arrays.stream(typeElement.getClass().getMethods()).filter(v -> v.isAnnotationPresent(Inherit.class));
//

        //String a = String.valueOf(Arrays.stream(typeElement.getClass().getFields()).map(f -> f.getAnnotation(Inheritance.class)).filter(Objects::nonNull).map(v -> v.classes()).findFirst());

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "AAAAAAAAAAA");
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, typeElement.asType().toString());
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, typeElement.getAnnotationMirrors().stream().map(Objects::toString).reduce((a, b) -> a + " " + b).orElse(null));

        //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, classes.toString());

//
//        Arrays.stream(element.getClass().getAnnotation(Inheritance.class).classes())
//                .peek(v -> processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, v.toString()))
//                .toList();

//                .get(0)
//                .getElementValues()
//                .values()
//                .stream()
//                .findFirst()
//                .stream()
//                .map(v -> {
//                    try {
//                        return Class.forName(v.toString());
//                    } catch (ClassNotFoundException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .map(Class::getSimpleName)
//                .toString());


//        List<FieldSpec> fields = typeElement.getAnnotationMirrors().get(0)
//                .getElementValues()
//                .values()
//                .stream()
//                .findFirst()
//                .map(v -> {
//                    try {
//                        return Class.forName(String.valueOf(v));
//                    } catch (ClassNotFoundException e) {
//                        throw new RuntimeException(e);
//                    }
//                })
//                .map(v -> FieldSpec.builder(v, v.toString())
//                        .addModifiers(PRIVATE)
//                        .build())
//                .stream().toList();
//
//        fields.forEach(implementationClass::addField);

        for (Element methodElement : typeElement.getEnclosedElements()) {
            if (methodElement.getKind() == ElementKind.METHOD) {
                MethodSpec.Builder methodBuilder = MethodSpec.overriding((ExecutableElement) methodElement)
                        .addModifiers(PUBLIC)
                        .addCode(CodeBlock.of("\n" +
                                "                System.out.println(\"hello\");"));
                // TODO: Add method body according to your requirements
                // You can use methodBuilder.addStatement("your code here");

                implementationClass.addMethod(methodBuilder.build());
            }
        }

        TypeSpec implementation = implementationClass.build();
        JavaFile javaFile = JavaFile.builder(packageName, implementation).build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
//
//        Set<? extends Element> classes = roundEnvironment
//                .getElementsAnnotatedWith(Inheritance.class);
//
//        classes.forEach(element -> {
//
//            try {
//                var annotation = element.getAnnotation(Inheritance.class);
//                String className = element.getSimpleName().toString();
//                Class<?> clazz = element.getClass();
//
//                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I'm 1");
//
//                JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(className + "Impll");
//                try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I'm 2");
//
//                    String packageName = Optional.of(className)
//                            .map(v -> {
//                                int lastDot = v.lastIndexOf(".");
//                                return v.substring(0, lastDot);
//                            })
//                            .orElseThrow(); //todo
//
//
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I'm 2");
//                    String classImplName = className + "Impl";
//                    String realParentClassName = "test";
//                    Class<?>[] pseudoParentsClassNames = annotation.classes();
//                    List<Method> methods = Arrays.stream(clazz
//                                    .getMethods())
//                            .filter(v -> v.isAnnotationPresent(Inherit.class))
//                            .toList();
//
//
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I'm 3");
//
//                    out.print("package %s;".formatted(packageName));
//                    out.print("public class %s extends %s".formatted(classImplName, realParentClassName));
//                    out.print("{");
//                    Arrays.stream(pseudoParentsClassNames)
//                            .peek(v -> {
//                                String upperName = v.getSimpleName(); //todo mb canonical
//                                String lowerName = Character.toLowerCase(upperName.charAt(0)) + upperName.substring(1);
//                                out.print("private %s %s;".formatted(upperName, lowerName));
//                            });
//
//
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I'm 4");
//
//                    methods.stream()
//                            .peek(method -> {
//                                String returnType = method.getReturnType().getCanonicalName();
//                                String methodName = method.getName();
//                                List<String> params = Arrays.stream(method.getParameters())
//                                        .map(Parameter::getName)
//                                        .toList(); //todo
//                                String content = "%s.%s(%s);".formatted(method.
//                                        getAnnotation(Inherit.class).from().getCanonicalName()); //todo
//                                out.print("@Override public %s %s(%s){%s;}".formatted());
//                            });
//
//
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I'm 5");
//
//                    out.print("@Override public %s %s(%s){%s;}"); //n times
//                    out.print("}");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
//        });
//
//
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I'm 6");
//
////        for (Element element : classes) {
////            var annotation = element.getAnnotation(Inheritance.class);
////            Class<?>[] parentClasses = annotation.classes();
////            Class<?> clazz = element.getClass();
////            List<Method> inheritedMethods = Arrays.stream(clazz.getMethods())
////                    .filter(v -> v.isAnnotationPresent(Inherit.class))
////                    .toList();
////
////            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I'm 7");
////
////            inheritedMethods.stream()
////                    .map(v -> v.getAnnotation(Inherit.class))
////                    .map(Inherit::from); //call method from that class
////
////            //todo extends
////        }
//
//        return true;
//    }

}
