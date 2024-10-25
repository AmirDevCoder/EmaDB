package com.emamagic.crud.postgres;

import com.emamagic.conf.EmaConfigData;
import com.emamagic.connection.ConnectionManager;
import com.emamagic.crud.base.EmaCommand;
import com.emamagic.crud.base.PostgresEmaCommand;
import com.emamagic.util.EntityMapperFactory;
import com.emamagic.util.ReflectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresReadCommand<T> extends PostgresEmaCommand<Class<T>, Optional<List<T>>> {

    public PostgresReadCommand(EmaConfigData config) {
        super(config);
    }

    @Override
    public Optional<List<T>> execute(Class<T> entityClass) {
        try {
            String query = "SELECT * FROM " + ReflectionUtil.findTableName(entityClass);
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            List<T> entities = new ArrayList<>();
            while (resultSet.next()) {
                entities.add(EntityMapperFactory.fromResultSet(resultSet).mapTo(entityClass));
            }

            return Optional.of(entities);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
