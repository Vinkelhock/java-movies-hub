package ru.practicum.moviehub.http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoviesHandler extends BaseHttpHandler {

    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private MoviesStore moviesStore;

    public MoviesHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

    private String extractYearParam(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("year=")) {
                return param.substring(5);
            }
        }
        return null;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        String path = exchange.getRequestURI().getPath();
        String[] pathList = path.split("/");

        if (method.equalsIgnoreCase("GET")) {
            methodGet(exchange, pathList);
        } else if (method.equalsIgnoreCase("POST")) {
            methodPost(exchange);
        } else if (method.equalsIgnoreCase("DELETE")) {
            methodDelete(exchange, pathList);
        }
    }

    private void methodGet(HttpExchange exchange, String[] pathList) throws IOException {
        try {
            if (pathList.length == 2) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null) {
                    String yearParam = extractYearParam(query);
                    if (yearParam != null) {
                        try {
                            int year = Integer.parseInt(yearParam);
                            sendJson(exchange, 200, gson.toJson(moviesStore.getMoviesByYear(year)));
                        } catch (NumberFormatException e) {
                            sendJson(exchange, 400, "{\n" +
                                    "     \"error\": \"Некорректный параметр запроса — year\",\n" +
                                    "   }");
                        }
                    } else {
                        sendJson(exchange, 200, gson.toJson(moviesStore.getMovies().values()));
                    }
                } else {
                    sendJson(exchange, 200, gson.toJson(moviesStore.getMovies().values()));
                }
            } else {
                int idValue = Integer.parseInt(pathList[2].trim()); // Пытаемся преобразовать
                HashMap<Integer, Movie> movies = moviesStore.getMovies();
                if (movies.containsKey(idValue)) {
                    sendJson(exchange, 200, gson.toJson(movies.get(idValue)));
                } else {
                    sendJson(exchange, 404, "{\n" +
                            "     \"error\": \"Фильм не найден\",\n" +
                            "   }");
                }
            }
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, "{\n" +
                    "     \"error\": \"Некорректный ID\",\n" +
                    "   }");
        } catch (Exception exception) {
            sendJson(exchange, 400, " {\n" +
                    "     \"error\": \"Ошибка вывода информации о фильмах\",\n" +
                    "   }");
        }
    }

    private void methodPost(HttpExchange exchange) throws IOException {
        Map<String, List<String>> headers = exchange.getRequestHeaders();
        if (headers.containsKey("Content-Type")) {
            List<String> contentTypes = headers.get("Content-Type");
            String contentType = contentTypes.get(0); // Получаем первое значение заголовка

            if (!contentType.equals("application/json; charset=UTF-8")) {
                sendJson(exchange, 415, "Данный тип данных не поддерживается");
                return;
            }
        } else {
            sendJson(exchange, 415, "");
            return;
        }

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            JsonElement jsonElement = JsonParser.parseString(requestBody);
            if (jsonElement.isJsonObject()) {
                Movie movie = gson.fromJson(requestBody, Movie.class);
                Movie.setId(movie);
                List<String> details = Movie.checkMovie(movie);

                if (details.isEmpty()) {
                    int id = moviesStore.addMovie(movie);
                    sendJson(exchange, 200, "{\"id\": " + id + "}");
                } else {
                    sendJson(exchange, 422, "{\"error\": \"Ошибка валидации\", \"details\": " + gson.toJson(details) + "}");
                }
            } else {
                sendJson(exchange, 422, "{\"error\": \"Ошибка добавления фильма\", \"details\": \"Данные переданы не в формате JSON\"}");
            }
        } catch (Exception exception) {
            sendJson(exchange, 422, " {\n" +
                    "     \"error\": \"Ошибка добавления фильма\",\n" +
                    "   }");
        }
    }

    private void methodDelete(HttpExchange exchange, String[] pathList) throws IOException {

        try {
            if (pathList.length == 3) {
                int idValue = Integer.parseInt(pathList[2].trim()); // Пытаемся преобразовать
                HashMap<Integer, Movie> movies = moviesStore.getMovies();
                if (movies.containsKey(idValue)) {
                    movies.remove(idValue);
                    sendJson(exchange, 204, "Фильм удален");
                } else {
                    sendJson(exchange, 404, "{\n" +
                            "     \"error\": \"Фильм не найден\",\n" +
                            "   }");
                }
            } else {
                sendJson(exchange, 400, "{\n" +
                        "     \"error\": \"Ошибка при удалении фильма\",\n" +
                        "   }");
            }
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, "{\n" +
                    "     \"error\": \"Некорректный ID\",\n" +
                    "   }");
        } catch (Exception exception) {
            sendJson(exchange, 400, "{\n" +
                    "     \"error\": \"Ошибка при удалении фильма\",\n" +
                    "   }");
        }
    }
}
