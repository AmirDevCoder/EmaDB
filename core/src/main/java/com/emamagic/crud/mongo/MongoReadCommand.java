package com.emamagic.crud.mongo;

import com.emamagic.conf.EmaConfigData;
import com.emamagic.connection.ConnectionManager;
import com.emamagic.crud.base.EmaCommand;
import com.emamagic.crud.base.MongoEmaCommand;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MongoReadCommand<T> extends MongoEmaCommand<Class<T>, Optional<List<T>>> {

    public MongoReadCommand(EmaConfigData config) {
        super(config);
    }

    @Override
    public Optional<List<T>> execute(Class<T> entityClass) {
        MongoCollection<Document> collection = database.getCollection(entityClass.getSimpleName().toLowerCase());

        throw new RuntimeException("Implement me");
    }

}
