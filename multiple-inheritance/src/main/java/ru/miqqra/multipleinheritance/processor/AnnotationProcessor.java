package ru.miqqra.multipleinheritance.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import ru.miqqra.multipleinheritance.annotations.Inherit;
import ru.miqqra.multipleinheritance.annotations.Inheritance;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("ru.miqqra.multipleinheritance.annotations.Inheritance")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I'm alive");
//
//        for (TypeElement element : annotations) {
//            if (element.getKind().equals(ElementKind.CLASS)) {
////                createImplementationFile(element);
//            }
//        }
//        return true;
//    }

    private void createImplementationFile(TypeElement typeElement) {
        String packageName = "org.example";
        String className = typeElement.getSimpleName().toString();

        TypeSpec.Builder implementationClass = TypeSpec.classBuilder(className + "Impl")
                .addModifiers(PUBLIC)
                .superclass(TypeName.get(typeElement.asType()));

//        //todo
//        Arrays.stream(typeElement.getClass().getMethods()).filter(v -> v.isAnnotationPresent(Inherit.class));
//
        List<FieldSpec> fields = Arrays.stream(typeElement
                        .getAnnotation(Inheritance.class)
                        .classes())
                .map(v -> FieldSpec.builder(v, v.getSimpleName())
                        .addModifiers(PRIVATE)
                        .build())
                .toList();
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


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {


        Set<? extends Element> classes = roundEnvironment
                .getElementsAnnotatedWith(Inheritance.class);

        classes.forEach(element -> {
            createImplementationFile((TypeElement) element);
        });

        return true;

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
