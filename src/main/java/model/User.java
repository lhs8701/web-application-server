package model;

import db.DataBase;

import java.util.Map;

import static service.UserService.*;
import static webserver.RequestHandler.*;

public class User {
    private String userId;
    private String password;
    private String name;
    private String email;

    public User(String userId, String password, String name, String email) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", password=" + password + ", name=" + name + ", email=" + email + "]";
    }

    public User(Map<String, String> params) {
        String userId = params.get(USER_ID_FIELD);
        String password = params.get(PASSWORD_FIELD);
        String name = params.get(NAME_FIELD);
        String email = params.get(EMAIL_FIELD);
        User user = new User(userId, password, name, email);
        DataBase.addUser(user);
    }
}
