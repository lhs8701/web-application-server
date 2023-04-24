package webserver;

import java.util.Arrays;

public enum RequestUrl {
    CREATE_USER("/user/create"),
    LOGIN("/user/login"),
    EMPTY("");

    private final String path;

    RequestUrl( String path) {
        this.path = path;
    }

    public String getPath(){
        return this.path;
    }

    static RequestUrl find(String path){
        return Arrays.stream(RequestUrl.values())
                .filter(e -> e.getPath().equals(path))
                .findFirst()
                .orElse(EMPTY);
    }
}
