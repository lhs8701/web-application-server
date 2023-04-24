package webserver.subtest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SplitTest {

    @Test
    @DisplayName("URI에서 url과 parameter를 분리한다")
    void test6() {
        // given
        String uri = "/user/create?userId=1&password=pwd&name=hello&email=hello@naver.com";
        Map<String, String> map = new HashMap<>();

        // when
        String[] token = uri.split("\\?|&");
        for (int i = 1; i < token.length; i++) {
            String[] elem = token[i].split("=");
            map.put(elem[0], elem[1]);
        }

        // then
        assertEquals("1", map.get("userId"));
        assertEquals("pwd", map.get("password"));
        assertEquals("hello", map.get("name"));
        assertEquals("hello@naver.com", map.get("email"));
    }
}
