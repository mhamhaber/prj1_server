package project1.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HelloServerTest {
    private HttpServer server;
    private URI endpoint;

    @BeforeEach
    void startServer() throws Exception {
        server = HelloServer.start(0);
        endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/hello");
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void servesGreetingAsJson() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(HttpRequest.newBuilder(endpoint).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertEquals("application/json; charset=utf-8",
                    response.headers().firstValue("Content-Type").orElseThrow());
            assertTrue(response.body().contains("\"title\": \"Hello, world!\""));
            assertTrue(response.body().contains("\"message\":"));
            assertTrue(response.body().contains("Java 25 server"));
        }
    }

    @Test
    void rejectsPost() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(HttpRequest.newBuilder(endpoint)
                    .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(405, response.statusCode());
            assertEquals("GET", response.headers().firstValue("Allow").orElseThrow());
        }
    }

    @Test
    void echoesTextWithUnicodeAndQueryCharacters() throws Exception {
        String text = "  Hello & café + 世界 🌍 = ?  ";
        try (var client = HttpClient.newHttpClient()) {
            var uri = URI.create(endpoint + "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8));
            var response = client.send(HttpRequest.newBuilder(uri).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("\"message\": \"" + text + "\""));
            assertTrue(response.body().contains("\"title\": \"Your response\""));
        }
    }

    @Test
    void escapesJsonSpecialCharacters() throws Exception {
        String text = "\"quoted\" \\ path\n\t\r\b\f\u0000";
        try (var client = HttpClient.newHttpClient()) {
            var uri = URI.create(endpoint + "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8));
            var response = client.send(HttpRequest.newBuilder(uri).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains(
                    "\"message\": \"\\\"quoted\\\" \\\\ path\\u000a\\u0009\\u000d\\u0008\\u000c\\u0000\""));
        }
    }

    @Test
    void echoesAnEmptyTextParameter() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(HttpRequest.newBuilder(URI.create(endpoint + "?text=")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("\"message\": \"\""));
        }
    }

    @Test
    void rejectsUnknownPath() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(HttpRequest.newBuilder(URI.create(endpoint + "/unknown"))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(404, response.statusCode());
        }
    }
}
