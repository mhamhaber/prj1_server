package project1.server;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
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
                body = """
                        {
                          "eyebrow": "A little connection. A world of possibility.",
                          "title": "Hello, world!",
                          "message": "Every great idea starts with a simple hello. This one travelled from a Java server to your screen. Imagine what comes next.",
                          "source": "Java 25 server"
                        }
                        """;
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
