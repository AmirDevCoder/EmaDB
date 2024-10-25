package com.emamagic;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class DBGenerator {


    static void generateDB(Set<String> qualifiedConfigName) {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        // TODO: configs must be distinct and one confWrapper acceptable for each DBType
//        for (MoPoConfigData confWrapper : moPoConfigDataSet) {
//            String returnStatement = String.format("return new MoPoConfigData(\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", DB.%s)",
//                    confWrapper.host(), confWrapper.port(), "test", confWrapper.username(), confWrapper.password(), confWrapper.db().name().toUpperCase());
//            methodSpecs.add(MethodSpec.methodBuilder(confWrapper.db().name().toLowerCase())
//                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                    .returns(MoPoConfigData.class)
//                    .addStatement(returnStatement)
//                    .build());
//        }
//
//        MethodSpec constructor = MethodSpec.constructorBuilder()
//                .addModifiers(Modifier.PRIVATE)
//                .addStatement("throw new $T()", UnsupportedOperationException.class)
//                .build();
//
//
//        TypeSpec moPoDBClass = TypeSpec.classBuilder("MoPoDB")
//                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//                .addMethod(constructor)
//                .addMethods(methodSpecs)
//                .addJavadoc("This is a utility class for database operations.\n")
//                .build();
        TypeSpec moPoDB = TypeSpec.classBuilder("MoPoDB")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(FieldSpec.builder(String.class, "className", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", qualifiedConfigName.stream().findFirst().get()) // Use className
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addStatement("throw new $T()", UnsupportedOperationException.class)
                        .build())
                .addMethod(generateMethod("mongo"))
                .build();

        JavaFile javaFile = JavaFile.builder("com.emamagic", moPoDB).build();

        try {
            // TODO: uses relative path instead of absolute path
            String absolutePath = "/home/emamagic/Developer/Java/MoPoDB/core/src/main/java";
            javaFile.writeTo(new File(absolutePath));
        } catch (IOException e) {
            // TODO: implement me
        }
    }

    private static MethodSpec generateMethod(String name) {
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.bestGuess("com.emamagic.conf.MoPoConfigData"))
                .beginControlFlow("try")
                .addStatement("Class<?> clazz = Class.forName(className)")
                .addStatement("Constructor<?> constructor = clazz.getConstructor()")
                .addStatement("var data = ($T) constructor.newInstance()", ClassName.bestGuess("com.emamagic.conf.MoPoConfig"))
                .addStatement("return new MoPoConfigData(data.getHost(), data.getPort(), data.getDBName(), data.getPassword(), data.getUsername(), DB.MONGODB)")
                .nextControlFlow("catch ($T e)", InvocationTargetException.class)
                .addStatement("return null")
                .nextControlFlow("catch ($T | $T | $T | $T e)", InstantiationException.class, IllegalAccessException.class, NoSuchMethodException.class, ClassNotFoundException.class)
                .addStatement("return null")
                .endControlFlow()
                .build();
    }


}
