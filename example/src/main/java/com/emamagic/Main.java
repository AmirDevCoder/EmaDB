package com.emamagic;

import com.emamagic.entity.Log;
import com.emamagic.entity.User;

import java.time.Instant;

public class Main {
    public static void main(String[] args) {
        System.out.println(EmaDB.upsert(getUser()));
        EmaDB.close();
    }

    private static User getUser() {
        var user = new User();
        user.setId(2);
        user.setName("marr");
        user.setEmail("mary@gmail.com");
        return user;
    }

    private static Log getLog() {
        var log = new Log();
        log.setAction("USER_CREATED");
        log.setCreatedAt(Instant.now());
        return log;
    }

}