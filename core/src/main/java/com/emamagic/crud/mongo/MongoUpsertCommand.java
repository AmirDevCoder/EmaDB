package com.emamagic.crud.mongo;

import com.emamagic.conf.EmaConfigData;
import com.emamagic.connection.ConnectionManager;
import com.emamagic.crud.base.EmaCommand;
import com.emamagic.crud.base.MongoEmaCommand;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.lang.reflect.Field;
import java.util.Optional;

public class MongoUpsertCommand<T> extends MongoEmaCommand<T, Optional<T>> {

    public MongoUpsertCommand(EmaConfigData config) {
        super(config);
    }

    // TODO: handle @EmaIgnoreRow and snake case field
    @Override
    public Optional<T> execute(T entity) {
        Class<?> clazz = entity.getClass();
        MongoCollection<Document> collection = database.getCollection(clazz.getSimpleName().toLowerCase());

        Document document = new Document();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                document.append(field.getName(), field.get(entity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing field " + field.getName(), e);
            }
        }

        collection.insertOne(document);
        return Optional.of(entity);
    }

}