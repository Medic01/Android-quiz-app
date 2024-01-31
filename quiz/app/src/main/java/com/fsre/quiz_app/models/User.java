package com.fsre.quiz_app.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String name;
    public String email;
    public String password;
    public Long points;
    public Boolean isAdmin;

    // Default (no-argument) constructor required for Firebase deserialization
    public User() {
    }

    public User(String name, String email, String password, Boolean isAdmin, Long points) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.points = points;
    }
}
