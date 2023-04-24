package webserver;

import java.util.Arrays;

public enum RequestUrl {
    CREATE_USER("GET","/user/create"),
    EMPTY("EMPTY", "");

    private final String method;
    private final String path;

    RequestUrl(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getPath(){
        return this.path;
    }

    public String getMethod() {
        return method;
    }

    static RequestUrl find(String method, String path){
        return Arrays.stream(RequestUrl.values())
                .filter(e -> e.getMethod().equals(method) && e.getPath().equals(path))
                .findFirst()
                .orElse(EMPTY);
    }
}
