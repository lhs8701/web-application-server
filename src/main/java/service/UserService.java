package service;

import db.DataBase;
import model.User;

import java.util.Map;

public class UserService {
    public static final String USER_ID_FIELD = "userId";
    public static final String PASSWORD_FIELD = "password";
    public static final String NAME_FIELD = "name";
    public static final String EMAIL_FIELD = "email";
    public static void createUser(Map<String, String> params){
        User user = new User(params);
        DataBase.addUser(user);
    }
}
