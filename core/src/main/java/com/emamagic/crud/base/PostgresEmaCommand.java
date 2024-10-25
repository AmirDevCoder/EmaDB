package com.emamagic.crud.base;

import com.emamagic.conf.EmaConfigData;
import com.emamagic.connection.ConnectionManager;

import java.sql.Connection;

public abstract class PostgresEmaCommand<T, R> implements EmaCommand<T, R> {
    protected Connection connection;

    public PostgresEmaCommand(EmaConfigData config) {
        connection = ConnectionManager.getInstance().getPostgresConnection(config);
    }
}
