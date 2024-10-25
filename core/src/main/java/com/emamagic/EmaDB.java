package com.emamagic;

import com.emamagic.annotation.Config;
import com.emamagic.annotation.Entity;
import com.emamagic.conf.DB;
import com.emamagic.conf.EmaConfig;
import com.emamagic.conf.EmaConfigData;
import com.emamagic.connection.ConnectionManager;
import com.emamagic.crud.base.EmaCommand;
import com.emamagic.crud.mongo.MongoDeleteCommand;
import com.emamagic.crud.mongo.MongoReadCommand;
import com.emamagic.crud.mongo.MongoUpsertCommand;
import com.emamagic.crud.postgres.PostgresDeleteCommand;
import com.emamagic.crud.postgres.PostgresReadCommand;
import com.emamagic.crud.postgres.PostgresUpsertCommand;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

// TODO: generate this class after all the configuration and validation are checked in compile-time
// TODO: EmaDB Library doesn't support id for exchanging
public class EmaDB {
    private static EmaConfigData postgresConfig;
    private static EmaConfigData mongoConfig;

    // TODO: handle custom exception all over the library
    static {
        ServiceLoader<EmaConfig> loader = ServiceLoader.load(EmaConfig.class);
        for (EmaConfig config : loader) {
            Class<?> configClass = config.getClass();
            // TODO: what if multiple configuration set for on type of DB
            if (configClass.isAnnotationPresent(Config.class)) {
                Config annotation = configClass.getAnnotation(Config.class);
                switch (annotation.db()) {
                    case DB.POSTGRESQL -> postgresConfig = new EmaConfigData(config, DB.POSTGRESQL);
                    case DB.MONGODB -> mongoConfig = new EmaConfigData(config, DB.MONGODB);
                }
            } else {
                throw new RuntimeException("No MoPoDB confWrapper found");
            }
        }
    }

    public static <T> Optional<T> upsert(T entity) {
        Entity entityAnnotation = entity.getClass().getAnnotation(Entity.class);
        EmaCommand<T, Optional<T>> upsertCommand = null;

        if (entityAnnotation.db() == DB.MONGODB) {
            upsertCommand = new MongoUpsertCommand<>(mongoConfig);
        } else if (entityAnnotation.db() == DB.POSTGRESQL) {
            upsertCommand = new PostgresUpsertCommand<>(postgresConfig);
        }

        if (upsertCommand != null) {
            return upsertCommand.execute(entity);
        } else {
            throw new UnsupportedOperationException("Unsupported database type");
        }
    }

    public static <T> boolean delete(T entity) {
        Entity entityAnnotation = entity.getClass().getAnnotation(Entity.class);
        EmaCommand<T, Boolean> deleteCommand = null;

        if (entityAnnotation.db() == DB.MONGODB) {
            deleteCommand = new MongoDeleteCommand<>(mongoConfig);
        } else if (entityAnnotation.db() == DB.POSTGRESQL) {
            deleteCommand = new PostgresDeleteCommand<>(postgresConfig);
        }

        if (deleteCommand != null) {
            return deleteCommand.execute(entity);
        } else {
            throw new RuntimeException("Unsupported database type");
        }

    }

    public static <T> Optional<List<T>> read(Class<T> entityType) {
        Entity entityAnnotation = entityType.getAnnotation(Entity.class);
        EmaCommand<Class<T>, Optional<List<T>>> readCommand = null;

        if (entityAnnotation.db() == DB.MONGODB) {
            readCommand = new MongoReadCommand<>(mongoConfig);
        } else if (entityAnnotation.db() == DB.POSTGRESQL) {
            readCommand = new PostgresReadCommand<>(postgresConfig);
        }

        if (readCommand != null) {
            return readCommand.execute(entityType);
        } else {
            throw new UnsupportedOperationException("Unsupported database type");
        }
    }

    public static void close() {
        ConnectionManager.getInstance().closeConnections();
    }

}
