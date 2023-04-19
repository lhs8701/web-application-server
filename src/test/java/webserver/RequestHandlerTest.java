package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestHandlerTest {
    @Test
    @DisplayName("InputStream의 read()를 이용하면 한 Byte를 읽을 수 있다.")
    void test1() throws IOException {
        // given
        String input = "hello";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());

        // when
        int result = in.read();

        // then
        assertEquals((char) result, 'h');
    }

    @Test
    @DisplayName("InputStream의 read()를 반복 호출하면 여러 Byte를 읽을 수 있다.")
    void test2() throws IOException {
        // given
        String input = "hello";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());

        // when
        StringBuilder sb = new StringBuilder();
        while (in.available() > 0) {
            int read = in.read();
            sb.append((char) read);
        }

        // then
        assertEquals(sb.toString(), input);
    }

    @Test
    @DisplayName("BufferedReader.readLine()을 이용하면 String을 읽을 수 있다.")
    void test3() throws IOException {
        // given
        String input = "hello";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(reader);

        // when
        String result = br.readLine();

        // then
        assertEquals(result, input);
    }

    @Test
    @DisplayName("BufferedReader.readLine()을 반복하면 여러 String을 읽을 수 있다.")
    void test4() throws IOException {
        // given
        String input = "hello\nworld\nhi";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(reader);

        // when
        String[] result = new String[3];
        String read;
        int idx = 0;
        while ((read = br.readLine()) != null && !read.isEmpty()) {
            result[idx++] = read;
        }

        // then
        assertEquals(result[0], "hello");
        assertEquals(result[1], "world");
        assertEquals(result[2], "hi");
    }

    @Test
    @DisplayName("BufferedReader를 사용하면 파일의 내용을 읽어올 수 있다. ")
    void test5() throws IOException {
        // given
        String path = "E:\\Spring\\web-application-server\\webapp\\index.html";
        File file = new File(path);
        assertTrue(file.exists());

        // when
        FileReader fr = new FileReader(path);
        BufferedReader bfr = new BufferedReader(fr);
        String str;
        while ((str = bfr.readLine()) != null && !str.isEmpty()) {
            System.out.println(str);
        }

        // then
    }
}