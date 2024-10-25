package com.emamagic.crud.base;

import com.emamagic.conf.EmaConfigData;
import com.emamagic.connection.ConnectionManager;
import com.mongodb.client.MongoDatabase;

public abstract class MongoEmaCommand<T, R> implements EmaCommand<T, R> {
    protected MongoDatabase database;

    public MongoEmaCommand(EmaConfigData config) {
        database = ConnectionManager.getInstance().getMongoDatabase(config);
    }
}
