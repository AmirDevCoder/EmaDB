package com.emamagic.util;

import com.emamagic.annotation.UniqueForUpdate;
import com.emamagic.annotation.Id;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TableCreator {
    private static TableCreator instance;
    private final Connection connection;

    private TableCreator(Connection connection) {
        this.connection = connection;
    }

    public static synchronized TableCreator getInstance(Connection connection) {
        if (instance == null) {
            instance = new TableCreator(connection);
        }
        return instance;
    }

    public <T> void createTableIfNotExist(T entity) throws SQLException, IllegalAccessException {
        Class<?> clazz = entity.getClass();
        Field[] fields = clazz.getDeclaredFields();

        String tableName = ReflectionUtil.findTableName(clazz);
        String idName = ReflectionUtil.findIdField(entity, clazz).name();

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append(" (")
                .append(idName)
                .append(" SERIAL PRIMARY KEY, ");

        for (Field field : fields) {
            if (ReflectionUtil.isIgnoreRow(field) || field.isAnnotationPresent(Id.class)) continue;

            String columnName = field.getName();
            String columnType = mapJavaTypeToSqlType(field.getType());
            queryBuilder.append(columnName)
                    .append(" ")
                    .append(columnType);

            if (field.isAnnotationPresent(UniqueForUpdate.class)) {
                queryBuilder.append(" UNIQUE");
            }

            queryBuilder.append(", ");
        }

        queryBuilder.setLength(queryBuilder.length() - 2);
        queryBuilder.append(")");

        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            stmt.executeUpdate();
        }
    }

    // TODO: supporting other types
    private String mapJavaTypeToSqlType(Class<?> type) {
        if (type == String.class) {
            return "VARCHAR(255)";
        } else if (type == int.class || type == Integer.class) {
            return "INTEGER";
        } else if (type == long.class || type == Long.class) {
            return "BIGINT";
        } else if (type == boolean.class || type == Boolean.class) {
            return "BOOLEAN";
        }
        return "TEXT";
    }
}

