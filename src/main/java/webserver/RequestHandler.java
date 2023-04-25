package webserver;

import model.User;
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
import java.util.List;
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
            resolveRequest(in, dos);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void resolveRequest(InputStream in, DataOutputStream dos) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        Map<String, String> header = readHeader(br);
        String[] element = parse(header.get(GENERAL));
        RequestMethod requestMethod = RequestMethod.find(element[0]);
        RequestUrl requestUrl = RequestUrl.find(element[1]);
        Map<String, String> requestParams = HttpRequestUtils.parseQueryString(element[2]);

        if (requestMethod == RequestMethod.GET) {
            if (requestUrl == RequestUrl.EMPTY) {
                byte[] data = getStaticFile(element[1]);
                response200Header(dos, data.length, null);
                responseBody(dos, data);
            }
            executeGet(header, requestUrl, requestParams, dos);
            return;
        }
        Map<String, String> body = readBody(br, Integer.parseInt(header.get("Content-Length")));
        executePost(body, requestUrl, requestParams, dos);
    }

    private void executeGet(Map<String, String> header, RequestUrl requestUrl, Map<String, String> requestParams, DataOutputStream dos) {
        if (requestUrl == RequestUrl.LIST) {
            Map<String, String> cookies = HttpRequestUtils.parseCookies(header.get("Cookie"));
            if (cookies != null && Boolean.parseBoolean(cookies.get("logined"))) {
                byte[] data = getUserList();
                response200Header(dos, data.length, null);
                responseBody(dos, data);
                return;
            }
            response302Header(dos, "/user/login.html", null);
        }
    }

    private byte[] getUserList() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html");
        sb.append("<html lang=\"ko\">");
        sb.append("<head>");
        sb.append("<meta charset=\"UTF-8\">");
        sb.append(" <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        sb.append("<title> 사용자 목록 </title>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<h1>사용자 목록</h1>");
        List<User> users = UserService.getUsers();
        for (User user : users) {
            sb.append("<div>");
            sb.append("아이디 : " + user.getUserId() + "<br>");
            sb.append("이름 : " + user.getName() + "<br>");
            sb.append("이메일 : " + user.getEmail() + "<br>");
            sb.append("</div>");
            sb.append("<br>");
        }
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void executePost(Map<String, String> body, RequestUrl requestUrl, Map<String, String> requestParams, DataOutputStream dos) {
        if (requestUrl == RequestUrl.CREATE_USER) {
            UserService.createUser(body);
            response302Header(dos, "/index.html", null);
            return;
        }
        if (requestUrl == RequestUrl.LOGIN) {
            boolean success = UserService.login(body);
            if (success) {
                response302Header(dos, "/index.html", "logined=true");
                return;
            }
            response302Header(dos, "/user/login_failed.html", "logined=false");
        }
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
        log.info(data);
        return HttpRequestUtils.parseQueryString(data);
    }

    private byte[] getStaticFile(String url) throws IOException {
        try {
            return Files.readAllBytes(new File(STATIC_DIR + url).toPath());
        } catch (NoSuchFileException e) {
            return new byte[0];
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            if (cookie != null) {
                dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            }
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String location, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Temporarily Moved \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Location : " + location + "\r\n");
            if (cookie != null) {
                dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            }
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
