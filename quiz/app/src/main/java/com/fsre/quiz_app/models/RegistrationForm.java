package com.fsre.quiz_app.models;

public class RegistrationForm {

    public String name;
    public String email;
    public String password;
    public Number points;
    public Boolean isAdmin;
    public RegistrationForm(String name, String email, String password, Number points, Boolean isAdmin) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.points = points;
        this.isAdmin = isAdmin;
    }
}
