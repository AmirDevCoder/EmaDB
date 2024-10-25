package com.emamagic.crud.mongo;

import com.emamagic.conf.EmaConfigData;
import com.emamagic.connection.ConnectionManager;
import com.emamagic.crud.base.EmaCommand;
import com.emamagic.crud.base.MongoEmaCommand;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

public class MongoDeleteCommand<T> extends MongoEmaCommand<T, Boolean> {

    public MongoDeleteCommand(EmaConfigData config) {
        super(config);
    }

    @Override
    public Boolean execute(T entity) {
        Class<?> clazz = entity.getClass();
        MongoCollection<Document> collection = database.getCollection(clazz.getSimpleName().toLowerCase());
        throw new RuntimeException("implement me");
    }

}
