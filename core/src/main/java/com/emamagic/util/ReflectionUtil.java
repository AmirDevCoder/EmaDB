package com.emamagic.util;

import com.emamagic.annotation.Entity;
import com.emamagic.annotation.Id;
import com.emamagic.annotation.IgnoreRow;
import com.emamagic.annotation.UniqueForUpdate;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public final class ReflectionUtil {

    private ReflectionUtil() {
    }


    public static void setQueryColumn(PreparedStatement stmt, List<Object> values) throws SQLException, IllegalAccessException {
        for (int i = 0; i < values.size(); i++) {
            stmt.setObject(i + 1, values.get(i));
        }
    }

    public static boolean isIgnoreRow(Field field) {
        return field.isAnnotationPresent(IgnoreRow.class);
    }

    public static boolean isIdRow(Field field) {
        return field.isAnnotationPresent(Id.class);
    }

    public static String findTableName(Class<?> clazz) {
        Entity entityAnnotation = clazz.getAnnotation(Entity.class);
        if (entityAnnotation.name().isBlank()) {
            return clazz.getSimpleName().toLowerCase().concat("s");
        }

        return entityAnnotation.name();
    }

    public static <T> UniqueForUpdateData findUniqueForUpdateField(T entity, Class<?> clazz) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(UniqueForUpdate.class)) {
                return new UniqueForUpdateData(field.getName(), field.get(entity));
            }
        }

        var idData = findIdField(entity, clazz);
        return new UniqueForUpdateData(idData.name(), idData.value());
    }

    public static boolean isUniqueForUpdateExist(Field[] fields) {
        for (Field field : fields) {
            if (field.isAnnotationPresent(UniqueForUpdate.class)) {
                return true;
            }
        }
        return false;
    }

    public static <T> IdData findIdField(T entity, Class<?> clazz) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)) {
                return new IdData(field.getName(), field.get(entity));
            }
        }

        throw new RuntimeException("no @Id found on fields in class: " + clazz.getSimpleName());
    }

    public record IdData(String name, Object value) {
    }

    public record UniqueForUpdateData(String name, Object value) {
    }
}
