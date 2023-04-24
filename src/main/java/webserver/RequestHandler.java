package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;
import util.HttpRequestUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final String CONNECTED_MESSAGE = "New Client Connect! Connected IP : {}, Port : {}";
    private Socket connection;
    public static final String STATIC_DIR = "./webapp";

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug(CONNECTED_MESSAGE, connection.getInetAddress(), connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = resolveRequest(in);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private byte[] resolveRequest(InputStream in) throws IOException {
        return executeLogic(parse(extractUrl(in)));
    }

    private byte[] executeLogic(String[] element) throws IOException {
        byte[] emptyBody = new byte[0];
        RequestUrl requestUrl = RequestUrl.find(element[0], element[1]);
        Map<String, String> requestParams = HttpRequestUtils.parseQueryString(element[2]);
        if (requestUrl == RequestUrl.EMPTY) {
            return getStaticFile(element[1]);
        }
        if (requestUrl == RequestUrl.CREATE_USER) {
            UserService.createUser(requestParams);
            return emptyBody;
        }
        return emptyBody;
    }

    private String[] parse(String requestInfo) {
        String[] result = new String[3];
        String[] s = requestInfo.split(" ");
        result[0] = s[0];
        int indexQuestion = s[1].indexOf("?");
        if (indexQuestion == -1) {
            result[1] = s[1];
            result[2] = null;
            return result;
        }
        result[1] = s[1].substring(0, indexQuestion);
        result[2] = s[1].substring(indexQuestion + 1);
        return result;
    }

    private String extractUrl(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder logMessage = new StringBuilder();
        String firstLine = br.readLine();
        logMessage.append(firstLine);
        printLog(br, logMessage);
        return firstLine;
    }

    private void printLog(BufferedReader br, StringBuilder logMessage) throws IOException {
        String input;
        while ((input = br.readLine()) != null && !input.isEmpty()) {
            logMessage.append(input).append("\n");
        }
        log.info(logMessage.toString());
    }

    private byte[] getStaticFile(String url) throws IOException {
        try {
            return Files.readAllBytes(new File(STATIC_DIR + url).toPath());
        } catch (NoSuchFileException e) {
            return new byte[0];
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
