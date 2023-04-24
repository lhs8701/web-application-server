package webserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestHandlerTest {
    @Test
    @DisplayName("요청 정보를 requestMethod, requestUrl, requestParam 세개로 구분한다.")
    void test() {
        // given
        String requestInfo = "GET /user/create?userId=javajigi&password=password";

        // when
        int indexBlank = requestInfo.indexOf(" ");
        String requestMethod = requestInfo.substring(0, indexBlank);
        String fullUrl = requestInfo.substring(indexBlank + 1);
        int indexQuestion = fullUrl.indexOf("?");
        String requestUrl = fullUrl.substring(0, indexQuestion);
        String requestParam = fullUrl.substring(indexQuestion + 1);

        // then
        assertEquals("GET", requestMethod);
        assertEquals("/user/create", requestUrl);
        assertEquals("userId=javajigi&password=password", requestParam);
    }
}