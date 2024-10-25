package com.emamagic.crud.postgres;

import com.emamagic.annotation.Id;
import com.emamagic.conf.EmaConfigData;
import com.emamagic.crud.base.PostgresEmaCommand;
import com.emamagic.util.EntityMapperFactory;
import com.emamagic.util.ReflectionUtil;
import com.emamagic.util.TableCreator;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class PostgresUpsertCommand<T> extends PostgresEmaCommand<T, Optional<T>> {

    public PostgresUpsertCommand(EmaConfigData config) {
        super(config);
    }

    // TODO: handle snake case field
    @Override
    public Optional<T> execute(T entity) {
        try {
            Class<?> clazz = entity.getClass();
            Field[] fields = clazz.getDeclaredFields();

            // TODO: check only once
            TableCreator.getInstance(connection).createTableIfNotExist(entity);

            boolean isExist = rowExistForIdUpdate(entity, clazz, fields) || rowExistForUniqueForUpdate(entity, clazz, fields);
            if (isExist) {
                return updateRow(entity, clazz, fields);
            } else {
                return insertRow(entity, clazz, fields);
            }


        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean rowExistForIdUpdate(T entity, Class<?> clazz, Field[] fields) throws SQLException, IllegalAccessException {
        // todo: only support int id
        Integer id = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)) {
                if (field.get(entity) == null) return false;
                id = (Integer) field.get(entity);
            }
        }
        String query = "select 1 from " + ReflectionUtil.findTableName(clazz) + " where id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, id);
            return stmt.executeQuery().next();
        }
    }

    private boolean rowExistForUniqueForUpdate(T entity, Class<?> clazz, Field[] fields) throws IllegalAccessException, SQLException {
        boolean isExist = ReflectionUtil.isUniqueForUpdateExist(fields);
        if (!isExist) return false;
        var uniqueForUpdateData = ReflectionUtil.findUniqueForUpdateField(entity, clazz);
        String query = "select 1 from " + ReflectionUtil.findTableName(clazz) + " where " + uniqueForUpdateData.name() + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, uniqueForUpdateData.value());
            return stmt.executeQuery().next();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<T> updateRow(T entity, Class<?> clazz, Field[] fields) throws SQLException, IllegalAccessException {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("update ")
                .append(ReflectionUtil.findTableName(clazz))
                .append(" set ");
        var conditions = Arrays.stream(fields)
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        return !ReflectionUtil.isIdRow(field) && field.get(entity) != null && !ReflectionUtil.isIgnoreRow(field);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }).map(field -> field.getName() + " = ?")
                .collect(Collectors.joining(", "));
        var uniqueForUpdateData = ReflectionUtil.findUniqueForUpdateField(entity, clazz);
        queryBuilder
                .append(conditions)
                .append(" where ")
                .append(uniqueForUpdateData.name())
                .append(" = ?")
                .append(" returning *");
        String query = queryBuilder.toString();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int counter = 1;
            for (Field field : fields) {
                field.setAccessible(true);
                if (!ReflectionUtil.isIdRow(field) && field.get(entity) != null && !ReflectionUtil.isIgnoreRow(field)) {
                    stmt.setObject(counter++, field.get(entity));
                }
            }
            stmt.setObject(counter, uniqueForUpdateData.value());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return Optional.of((T) EntityMapperFactory.fromResultSet(resultSet).mapTo(entity.getClass()));
            }
            throw new RuntimeException("update operation failed");
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<T> insertRow(T entity, Class<?> clazz, Field[] fields) throws SQLException, IllegalAccessException {
        // todo: find better way to implement checked-exception inside stream pipeline
        String placeholder = Arrays.stream(fields)
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        return !ReflectionUtil.isIdRow(field) && field.get(entity) != null && !ReflectionUtil.isIgnoreRow(field);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(field -> "?")
                .collect(Collectors.joining(", "));

        String filedNames = Arrays.stream(fields)
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        return !ReflectionUtil.isIdRow(field) && field.get(entity) != null && !ReflectionUtil.isIgnoreRow(field);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(Field::getName)
                .collect(Collectors.joining(", "));

        String query = "insert into " + ReflectionUtil.findTableName(clazz) + " (" + filedNames + ") " + " values (" + placeholder + ") returning *";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int counter = 1;
            for (Field field : fields) {
                if (!ReflectionUtil.isIdRow(field) && field.get(entity) != null && !ReflectionUtil.isIgnoreRow(field)) {
                    stmt.setObject(counter++, field.get(entity));
                }
            }

            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return Optional.of((T) EntityMapperFactory.fromResultSet(resultSet).mapTo(entity.getClass()));
            }

            throw new RuntimeException("insertion operation filed");
        }
    }

}
