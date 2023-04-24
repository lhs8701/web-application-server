package service;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.util.Map;

public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    public static final String USER_ID_FIELD = "userId";
    public static final String PASSWORD_FIELD = "password";
    public static final String NAME_FIELD = "name";
    public static final String EMAIL_FIELD = "email";
    public static void createUser(Map<String, String> params){
        String userId = params.get(USER_ID_FIELD);
        String password = params.get(PASSWORD_FIELD);
        String name = params.get(NAME_FIELD);
        String email = params.get(EMAIL_FIELD);

        User user = new User(userId, password, name, email);
        DataBase.addUser(user);
        log.info("user saved : {}", user);
    }
}
