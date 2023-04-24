package webserver.subtest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrimTest {
    @Test
    @DisplayName("trim()은 앞뒤 공백을 제거한다.")
    void test() {
        // given
        String str = " test ";
        // when
        String result = str.trim();
        // then
        assertEquals("test", result);
    }
}
