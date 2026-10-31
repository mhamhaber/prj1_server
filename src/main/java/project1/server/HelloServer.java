package project1.server;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class HelloServer {
    private HelloServer() {}

    public static HttpServer start(int port) throws IOException {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/hello", exchange -> {
            int status;
            String body;
            if (!exchange.getRequestURI().getPath().equals("/api/hello")) {
                status = 404;
                body = "{\"error\":\"Not found\"}";
            } else if (!exchange.getRequestMethod().equals("GET")) {
                status = 405;
                exchange.getResponseHeaders().set("Allow", "GET");
                body = "{\"error\":\"Method not allowed\"}";
            } else {
                status = 200;
                String text = null;
                String query = exchange.getRequestURI().getRawQuery();
                try {
                    if (query != null) {
                        for (String parameter : query.split("&")) {
                            String[] pair = parameter.split("=", 2);
                            if (URLDecoder.decode(pair[0], StandardCharsets.UTF_8).equals("text")) {
                                text = URLDecoder.decode(pair.length == 2 ? pair[1] : "", StandardCharsets.UTF_8);
                                break;
                            }
                        }
                    }
                } catch (IllegalArgumentException invalidEncoding) {
                    status = 400;
                }
                body = """
                        {
                          "eyebrow": "A little connection. A world of possibility.",
                          "title": "Hello, world!",
                          "message": "Every great idea starts with a simple hello. This one travelled from a Java server to your screen. Imagine what comes next.",
                          "source": "Java 25 server"
                        }
                        """;
                if (status == 400) {
                    body = "{\"error\":\"Invalid query encoding\"}";
                } else if (text != null) {
                    body = """
                            {
                              "eyebrow": "Your words, echoed by the server.",
                              "title": "Your response",
                              "message": %s,
                              "source": "Java 25 server"
                            }
                            """.formatted(jsonString(text));
                }
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(status, bytes.length);
            try (var response = exchange.getResponseBody()) {
                response.write(bytes);
            } finally {
                exchange.close();
            }
        });
        server.start();
        return server;
    }

    private static String jsonString(String value) {
        var escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.append('"').toString();
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        var server = start(port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down; allowing active requests up to 3 seconds to finish.");
            server.stop(3);
        }));
        System.out.println("Hello API listening on http://127.0.0.1:" + server.getAddress().getPort());
    }
}
