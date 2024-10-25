package com.emamagic.util;

import com.emamagic.annotation.Id;
import com.emamagic.exception.DataMappingException;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class EntityMapperFactory {
    private final ResultSet resultSet;

    private EntityMapperFactory(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public static EntityMapperFactory fromResultSet(ResultSet resultSet) {
        return new EntityMapperFactory(resultSet);
    }

    public <T> T mapTo(Class<T> type) {
        try {
            var instance = type.getDeclaredConstructor().newInstance();
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (ReflectionUtil.isIgnoreRow(field)) { continue; }
                field.setAccessible(true);
                String snakeCaseFieldName = convertCamelCaseToSnakeCase(field.getName());
                try {
                    Object value = resultSet.getObject(snakeCaseFieldName);
                    if (value != null) {
                        field.set(instance, value);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException( "column not found for field so skipping field:  " + snakeCaseFieldName, e);
                }
            }
            return type.cast(instance);
        } catch (Exception e) {
            if (e instanceof NoSuchMethodException) {
                // TODO: it could be checked in processor
                throw new DataMappingException(type.getSimpleName().concat(" entity needs empty-constructor"), null);
            }
            throw new DataMappingException("DataMapper has a problem", e);
        }
    }

    // TODO: we can add different converter supper and custom converter
    private static String convertCamelCaseToSnakeCase(String camelCase) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            if (String.valueOf(camelCase.charAt(i)).equals(String.valueOf(camelCase.charAt(i)).toUpperCase())) {
                String lowerCase = String.valueOf(camelCase.charAt(i)).toLowerCase();
                if (i == 0) {
                    result.append(lowerCase);
                } else {
                    result.append("_").append(lowerCase);
                }
            } else {
                result.append(camelCase.charAt(i));
            }
        }

        return result.toString();
    }

}
