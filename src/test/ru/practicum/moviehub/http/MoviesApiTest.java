package ru.practicum.moviehub.http;

import org.junit.jupiter.api.*;
import ru.practicum.moviehub.store.MoviesStore;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MoviesApiTest {

    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {        // Создаём сервер
        server = new MoviesServer(new MoviesStore(), 8080);
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    @Order(1)
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    @Order(2)
    void addMovies_first_withError_year1701() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Гарри Поттер и философский камень\", \"year\": 1701}"))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertEquals("{\"error\": \"Ошибка валидации\", \"details\": [\n" +
                        "  \"Год фильма должен быть между 1888 и 2027\"\n" +
                        "]}", body,
                "Ответ должен содержать информацию об ошибках");

    }

    @Test
    @Order(3)
    void addMovies_first_withError_emptyTitle() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": null, \"year\": 2001}"))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertEquals("{\"error\": \"Ошибка валидации\", \"details\": [\n" +
                        "  \"Название фильма не может быть пустым\"\n" +
                        "]}", body,
                "Ответ должен содержать информацию об ошибках");
    }

    @Test
    @Order(4)
    void addMovies_first_withError_TitleMore100_and_year2055() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Гарри Поттер и философский камень Гарри Поттер и философский камень Гарри Поттер и философский камень\", \"year\": 2055}"))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertEquals("{\"error\": \"Ошибка валидации\", \"details\": [\n" +
                        "  \"Название фильма не может быть длиннее 100 символов\",\n  \"Год фильма должен быть между 1888 и 2027\"\n" +
                        "]}", body,
                "Ответ должен содержать информацию об ошибках");
    }

    @Test
    @Order(5)
    void addMovies_first_withError_contentType_xml() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "text/html; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Гарри Поттер и философский камень Гарри Поттер и философский камень Гарри Поттер и философский камень\", \"year\": 2055}"))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp.statusCode(), "POST /movies должен вернуть 415");
    }

    @Test
    @Order(6)
    void addMovies_first_withError_contentType_empty() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Гарри Поттер и философский камень Гарри Поттер и философский камень Гарри Поттер и философский камень\", \"year\": 2055}"))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp.statusCode(), "POST /movies должен вернуть 415");
    }

    @Test
    @Order(7)
    void addMovies_first_withError_json() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"year\": 2055}"))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertEquals("{\n" +
                        "     \"error\": \"Ошибка добавления фильма\",\n" +
                        "   }", body,
                "Ответ должен содержать информацию об ошибках");
    }

    @Test
    @Order(8)
    void addMovies_first_withSuccess_Harry_year2001() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Гарри Поттер и философский камень\", \"year\": 2001}"))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "POST /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("{\"id\":") && body.endsWith("}"),
                "Ответ должен содержать id созданной записи");
    }

    @Test
    @Order(9)
    void getMovies_whenOneMovie_returnsArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");

        assertTrue(body.startsWith("[\n" +
                        "  {\n" +
                        "    \"id\": ") && body.endsWith("    \"title\": \"Гарри Поттер и философский камень\",\n" +
                        "    \"year\": 2001\n" +
                        "  }\n" +
                        "]"),
                "Ответ должен содержать информацию о первой книге");

    }

    @Test
    @Order(10)
    void getMovies_when_ID_2() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Гарри Поттер и Тайная комната\", \"year\": 2002}"))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Гарри Поттер и узник Азкабана\", \"year\": 2004}"))
                .build();

        resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/5"))
                .GET()
                .build();

        resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertEquals("{\n" +
                        "  \"id\": 5,\n" +
                        "  \"title\": \"Гарри Поттер и Тайная комната\",\n" +
                        "  \"year\": 2002\n" +
                        "}", body,
                "Ответ должен содержать данные о второй книге");
    }

    @Test
    @Order(11)
    void getMovies_when_ID_777() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/777"))
                .GET()
                .build();

        HttpResponse resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "GET /movies должен вернуть 400");


        String body = resp.body().toString();
        assertEquals("{\n" +
                        "     \"error\": \"Фильм не найден\",\n" +
                        "   }", body,
                "Должна выводиться ошибка о том, что фильм не найден");
    }

    @Test
    @Order(12)
    void getMovies_when_ID_olga13() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/olga13"))
                .GET()
                .build();

        HttpResponse resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies должен вернуть 404");


        String body = resp.body().toString();
        assertEquals("{\n" +
                        "     \"error\": \"Некорректный ID\",\n" +
                        "   }", body,
                "Должна выводиться ошибка о некорректном значении id");
    }

    @Test
    @Order(13)
    void deleteMovies_when_ID_6() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/6"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, resp.statusCode(), "DELETE /movies должен вернуть 204");
    }

    @Test
    @Order(14)
    void deleteMovies_when_ID_777() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/777"))
                .DELETE()
                .build();

        HttpResponse resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "DELETE /movies должен вернуть 404");


        String body = resp.body().toString();
        assertEquals("{\n" +
                        "     \"error\": \"Фильм не найден\",\n" +
                        "   }", body,
                "Должна выводиться ошибка о том, что фильм не найден");
    }

    @Test
    @Order(15)
    void deleteMovies_when_ID_olga13() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/olga13"))
                .DELETE()
                .build();

        HttpResponse resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "DELETE /movies должен вернуть 400");


        String body = resp.body().toString();
        assertEquals("{\n" +
                        "     \"error\": \"Некорректный ID\",\n" +
                        "   }", body,
                "Должна выводиться ошибка о некорректном значении id");
    }

    @Test
    @Order(16)
    void getMovies_withParam_year_2002() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2002"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String body = resp.body().trim();
        assertEquals("[\n" +
                        "  {\n" +
                        "    \"id\": 5,\n" +
                        "    \"title\": \"Гарри Поттер и Тайная комната\",\n" +
                        "    \"year\": 2002\n" +
                        "  }\n" +
                        "]", body,
                "Ответ должен содержать данные о второй книге");
    }

    @Test
    @Order(17)
    void getMovies_withParam_year_777() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=777"))
                .GET()
                .build();

        HttpResponse resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");


        String body = resp.body().toString();
        assertEquals("[]", body,
                "Должен выводиться пустой список");
    }

    @Test
    @Order(18)
    void getMovies_withParam_year_olga13() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=olga13"))
                .GET()
                .build();

        HttpResponse resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies должен вернуть 400");


        String body = resp.body().toString();
        assertEquals("{\n" +
                        "     \"error\": \"Некорректный параметр запроса — year\",\n" +
                        "   }", body,
                "Должна выводиться ошибка о некорректном параметре запроса");
    }
}