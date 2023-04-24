package webserver;

import java.util.Arrays;

public enum RequestMethod {
    GET("GET"),
    EMPTY("EMPTY");

    final String name;

    RequestMethod(String name) {
        this.name = name;
    }

    private String getName() {
        return this.name;
    }

    static RequestMethod findByName(String name) {
        return Arrays.stream(RequestMethod.values())
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }
}
