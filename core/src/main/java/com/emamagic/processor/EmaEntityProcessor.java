package com.emamagic.processor;

import com.emamagic.annotation.Entity;
import com.emamagic.annotation.Id;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.emamagic.annotation.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class EmaEntityProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        boolean hasErrors = false;
        for (Element element: roundEnvironment.getElementsAnnotatedWith(Entity.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@Entity can only be applied to classes", element);
                hasErrors = true;
                continue;
            }

            TypeElement classElement = (TypeElement) element;
            List<? extends Element> enclosedElements = classElement.getEnclosedElements();

            int idFieldCount = 0;
            for (Element e : enclosedElements) {
                if (e.getKind() == ElementKind.FIELD && e.getAnnotation(Id.class) != null) {
                    idFieldCount++;
                }
            }
            if (idFieldCount != 1) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Classes annotated with @Entity must have exactly one field annotated with @Id", element);
                hasErrors = true;
            }

            // TODO: you can use the "org.objenesis" library which is allows you to create objects without calling any constructors
            boolean hasNoArgsConstructor = false;
            for (Element enclosedElement : enclosedElements) {
                if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                    ExecutableElement constructorElement = (ExecutableElement) enclosedElement;
                    if (constructorElement.getParameters().isEmpty()) {
                        hasNoArgsConstructor = true;
                        break;
                    }
                }
            }

            if (!hasNoArgsConstructor) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Class annotated with @Entity must have a no-argument constructor", element);
                hasErrors = true;
            }
        }

        return hasErrors;
    }

}
