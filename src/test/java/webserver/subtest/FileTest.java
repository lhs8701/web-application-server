package webserver.subtest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

public class FileTest {
    @Test
    @DisplayName("존재하지 않는 파일인 경우 NoSuchFileException예외가 발생한다.")
    void test() throws IOException {
        // given
        File file = new File("invalid");

        // when
        Assertions.assertThrows(NoSuchFileException.class, () -> {
            byte[] bytes = Files.readAllBytes(file.toPath());
        });

        // then
    }
}
