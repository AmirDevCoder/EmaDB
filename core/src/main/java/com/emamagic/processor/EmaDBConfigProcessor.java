package com.emamagic.processor;

import com.emamagic.annotation.Config;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.emamagic.annotation.Config")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class EmaDBConfigProcessor extends AbstractProcessor {
    private static final String SERVICE_FILE = "META-INF/services/com.emamagic.conf.EmaConfig";
    private final List<String> configClassNames = new ArrayList<>();

    /**
     * @return True: The processor claims that it has fully handled the annotations it was responsible for. No other annotation processors need to process those annotations.
     * False: The processor indicates that it has not fully processed the annotations, leaving the possibility for other processors to process the same annotations.
     * TypeElement is a representation of a class, interface, enum, or annotation type
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Config.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR, "Only classes can be annotated with @Config", element);
                return true;
            }

            if (!isSubclassOfDBConfigAbstraction(element)) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR, element.getSimpleName() +
                                " must implement MoPoConfig to use @Config", element);
                return true;
            }

            TypeElement classElement = (TypeElement) element;
            String className = classElement.getQualifiedName().toString();
            configClassNames.add(className);
        }

        try {
            generateServiceFile(configClassNames);
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }

        return true;
    }

    private boolean isSubclassOfDBConfigAbstraction(Element type) {
        TypeElement superclass = processingEnv.getElementUtils().getTypeElement("com.emamagic.conf.EmaConfig");
        return processingEnv.getTypeUtils().isSubtype(type.asType(), superclass.asType());
    }

    private void generateServiceFile(List<String> classNames) throws IOException {
        Filer filer = processingEnv.getFiler();
        FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", SERVICE_FILE);
        try (Writer writer = resource.openWriter()) {
            for (String className : classNames) {
                writer.write(className + "\n");
            }
        }
    }
}
