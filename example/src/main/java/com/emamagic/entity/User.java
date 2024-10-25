package com.emamagic.entity;

import com.emamagic.annotation.UniqueForUpdate;
import com.emamagic.annotation.Entity;
import com.emamagic.annotation.Id;
import com.emamagic.conf.DB;

// needs no-args constructor
@Entity(db = DB.POSTGRESQL, name = "users")
public class User {
    @Id
    private Integer id;
    private String name;
    @UniqueForUpdate
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
