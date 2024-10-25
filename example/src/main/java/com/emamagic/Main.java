package com.emamagic;

import com.emamagic.entity.Log;
import com.emamagic.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        // Insert or update a user record
        Optional<User> upsertedUser = EmaDB.upsert(getUser());

        // Delete records
        boolean isDeleted = EmaDB.delete(getUser());

        // Read records
        Optional<List<User>> users = EmaDB.read(User.class);

        // Close the database connection
        EmaDB.close();
    }

    private static User getUser() {
        var user = new User();
        user.setName("ali");
        user.setEmail("ali@gmail.com");
        return user;
    }

    private static Log getLog() {
        var log = new Log();
        log.setAction("USER_CREATED");
        log.setCreatedAt(Instant.now());
        return log;
    }

}