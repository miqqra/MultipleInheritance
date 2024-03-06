package ru.miqqra.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("ru.miqqra.annotations.Inheritance")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class AnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I'm alive");

        for (TypeElement element : annotations) {
            if (element.getKind().equals(ElementKind.CLASS)) {
//                createImplementationFile(element);
            }
        }
        return true;
    }

//    private void createImplementationFile(TypeElement typeElement) {
//        String packageName = "org.example";
//        String className = typeElement.getSimpleName().toString();
//
//        TypeSpec.Builder implementationClass = TypeSpec.classBuilder(className + "Impl")
//                .addModifiers(PUBLIC, FINAL)
//                .superclass(TypeName.get(typeElement.asType()));
//
//        for (Element methodElement : typeElement.getEnclosedElements()) {
//            if (methodElement.getKind() == ElementKind.METHOD) {
//                MethodSpec.Builder methodBuilder = MethodSpec.overriding((ExecutableElement) methodElement)
//                        .addModifiers(PUBLIC);
//
//                // TODO: Add method body according to your requirements
//                // You can use methodBuilder.addStatement("your code here");
//
//                implementationClass.addMethod(methodBuilder.build());
//            }
//        }
//
//        TypeSpec implementation = implementationClass.build();
//        JavaFile javaFile = JavaFile.builder(packageName, implementation).build();
//
//        try {
//            javaFile.writeTo(processingEnv.getFiler());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public boolean process1(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
//
//        Set<? extends Element> classes = roundEnvironment
//                .getElementsAnnotatedWith(Inheritance.class);
//
//        classes.forEach(element -> {
//            try {
//                var annotation = element.getAnnotation(Inheritance.class);
//                String className = element.getSimpleName().toString();
//                Class<?> clazz = element.getClass();
//
//                JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(className + "Impll");
//                try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
//                    String packageName = Optional.of(className)
//                            .map(v -> {
//                                int lastDot = v.lastIndexOf(".");
//                                return v.substring(0, lastDot);
//                            })
//                            .orElseThrow(); //todo
//                    String classImplName = className + "Impl";
//                    String realParentClassName = "test";
//                    Class<?>[] pseudoParentsClassNames = annotation.classes();
//                    List<Method> methods = Arrays.stream(clazz
//                                    .getMethods())
//                            .filter(v -> v.isAnnotationPresent(Inherit.class))
//                            .toList();
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
//                    methods.stream()
//                            .peek(method -> {
//                                String returnType = method.getReturnType().getCanonicalName();
//                                String methodName = method.getName();
//                                List<String> params = Arrays.stream(method.getParameters()).map(Parameter::getName).toList(); //todo
//                                String content = "%s.%s(%s);".formatted(method.
//                                        getAnnotation(Inherit.class).from().getCanonicalName()); //todo
//                                out.print("@Override public %s %s(%s){%s;}".formatted());
//                            });
//                    out.print("@Override public %s %s(%s){%s;}"); //n times
//                    out.print("}");
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//        for (Element element : classes) {
//            var annotation = element.getAnnotation(Inheritance.class);
//            Class<?>[] parentClasses = annotation.classes();
//            Class<?> clazz = element.getClass();
//            List<Method> inheritedMethods = Arrays.stream(clazz.getMethods())
//                    .filter(v -> v.isAnnotationPresent(Inherit.class))
//                    .toList();
//            inheritedMethods.stream()
//                    .map(v -> v.getAnnotation(Inherit.class))
//                    .map(Inherit::from); //call method from that class
//
//            //todo extends
//        }
//
//        return true;
//    }

}
