package com.emamagic.crud.postgres;

import com.emamagic.conf.EmaConfigData;
import com.emamagic.crud.base.PostgresEmaCommand;
import com.emamagic.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/// Selective Field Matching (Nullable Fields Ignored):
public class PostgresDeleteCommand<T> extends PostgresEmaCommand<T, Boolean> {

    public PostgresDeleteCommand(EmaConfigData config) {
        super(config);
    }


    @Override
    public Boolean execute(T entity) {
        try {
            Class<?> clazz = entity.getClass();
            Field[] fields = clazz.getDeclaredFields();
            DeletePair deletePair = delete(entity, fields, clazz);
            PreparedStatement statement = connection.prepareStatement(deletePair.query());
            ReflectionUtil.setQueryColumn(statement, deletePair.params());
            return statement.executeUpdate() > 0;
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private DeletePair delete(T entity, Field[] fields, Class<?> clazz) {
        StringBuilder whereClause = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        for (Field field : fields) {
            if (ReflectionUtil.isIgnoreRow(field)) continue;
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value != null) {
                    if (!whereClause.isEmpty()) {
                        whereClause.append(" AND ");
                    }
                    whereClause.append(field.getName()).append(" = ?");
                    parameters.add(value);
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing field " + field.getName(), e);
            }
        }

        if (!whereClause.isEmpty()) {
            String query = "DELETE FROM " + ReflectionUtil.findTableName(clazz) + " WHERE " + whereClause;
            return new DeletePair(query, parameters);
        } else {
            throw new IllegalArgumentException("No fields specified for deletion");
        }
    }

    record DeletePair(String query, List<Object> params){}
}
