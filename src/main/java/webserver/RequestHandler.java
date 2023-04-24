package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final String CONNECTED_MESSAGE = "New Client Connect! Connected IP : {}, Port : {}";
    private Socket connection;
    public static final String STATIC_DIR = "./webapp";
    public static final String GENERAL = "general";

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
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        Map<String, String> header = readHeader(br);
        String[] element = parse(header.get(GENERAL));
        RequestMethod requestMethod = RequestMethod.find(element[0]);
        RequestUrl requestUrl = RequestUrl.find(element[1]);
        Map<String, String> requestParams = HttpRequestUtils.parseQueryString(element[2]);

        if (requestMethod == RequestMethod.GET) {
            if (requestUrl == RequestUrl.EMPTY) {
                return getStaticFile(element[1]);
            }
            return executeGet(requestUrl, requestParams);
        }
        Map<String, String> body = readBody(br, Integer.parseInt(header.get("Content-Length")));
        return executePost(body, requestUrl, requestParams);
    }

    private byte[] executeGet(RequestUrl requestUrl, Map<String, String> requestParams) {
        return new byte[0];
    }

    private byte[] executePost(Map<String, String> body, RequestUrl requestUrl, Map<String, String> requestParams) {
        if (requestUrl == RequestUrl.CREATE_USER) {
            UserService.createUser(body);
            return new byte[0];
        }
        return new byte[0];
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

    private Map<String, String> readHeader(BufferedReader br) throws IOException {
        Map<String, String> header = new HashMap<>();
        readFirstLine(br, header);
        readOtherLines(br, header);
        return header;
    }

    private void readFirstLine(BufferedReader br, Map<String, String> header) throws IOException {
        String input = br.readLine();
        log.info(input);
        header.put(GENERAL, input);
    }

    private void readOtherLines(BufferedReader br, Map<String, String> header) throws IOException {
        StringBuilder logMessage = new StringBuilder();
        String input;
        while ((input = br.readLine()) != null && !input.isEmpty()) {
            fillHeaderMap(input, header);
            logMessage.append(input).append("\n");
        }
        log.info(logMessage.toString());
    }

    private void fillHeaderMap(String input, Map<String, String> header) {
        int index = input.indexOf(":");
        String key = input.substring(0, index);
        String value = input.substring(index + 1).trim();
        header.put(key, value);
    }

    private Map<String, String> readBody(BufferedReader br, int contentLength) throws IOException {
        if (contentLength == 0) {
            return null;
        }
        String data = IOUtils.readData(br, contentLength + 1);
        return HttpRequestUtils.parseQueryString(data);
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
