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
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        String path = ex.getRequestURI().getPath();
        String[] pathList = path.split("/");

        if (method.equalsIgnoreCase("GET")) {
            methodGet(ex, pathList);
        } else if (method.equalsIgnoreCase("POST")) {
            methodPost(ex);
        } else if (method.equalsIgnoreCase("DELETE")) {
            methodDelete(ex, pathList);
        }
    }

    private void methodGet(HttpExchange ex, String[] pathList) throws IOException {
        try {
            if (pathList.length == 2) {
                String query = ex.getRequestURI().getQuery();
                if (query != null) {
                    String yearParam = extractYearParam(query);
                    if (yearParam != null) {
                        try {
                            int year = Integer.parseInt(yearParam);
                            sendJson(ex, 200, gson.toJson(moviesStore.getMoviesByYear(year)));
                        } catch (NumberFormatException e) {
                            sendJson(ex, 400, "{\n" +
                                    "     \"error\": \"Некорректный параметр запроса — year\",\n" +
                                    "   }");
                        }
                    } else {
                        sendJson(ex, 200, gson.toJson(moviesStore.getMovies().values()));
                    }
                } else {
                    sendJson(ex, 200, gson.toJson(moviesStore.getMovies().values()));
                }
            } else {
                int idValue = Integer.parseInt(pathList[2].trim()); // Пытаемся преобразовать
                HashMap<Integer, Movie> movies = moviesStore.getMovies();
                if (movies.containsKey(idValue)) {
                    sendJson(ex, 200, gson.toJson(movies.get(idValue)));
                } else {
                    sendJson(ex, 404, "{\n" +
                            "     \"error\": \"Фильм не найден\",\n" +
                            "   }");
                }
            }
        } catch (NumberFormatException e) {
            sendJson(ex, 400, "{\n" +
                    "     \"error\": \"Некорректный ID\",\n" +
                    "   }");
        } catch (Exception exception) {
            sendJson(ex, 400, " {\n" +
                    "     \"error\": \"Ошибка вывода информации о фильмах\",\n" +
                    "   }");
        }
    }

    private void methodPost(HttpExchange ex) throws IOException {
        Map<String, List<String>> headers = ex.getRequestHeaders();
        if (headers.containsKey("Content-Type")) {
            List<String> contentTypes = headers.get("Content-Type");
            String contentType = contentTypes.get(0); // Получаем первое значение заголовка

            if (!contentType.equals("application/json; charset=UTF-8")) {
                sendJson(ex, 415, "Данный тип данных не поддерживается");
                return;
            }
        } else {
            sendJson(ex, 415, "");
            return;
        }

        String requestBody = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            JsonElement jsonElement = JsonParser.parseString(requestBody);
            if (jsonElement.isJsonObject()) {
                Movie movie = gson.fromJson(requestBody, Movie.class);
                Movie.setId(movie);
                List<String> details = Movie.checkMovie(movie);

                if (details.isEmpty()) {
                    int id = moviesStore.addMovie(movie);
                    sendJson(ex, 200, "{\"id\": " + id + "}");
                } else {
                    sendJson(ex, 422, "{\"error\": \"Ошибка валидации\", \"details\": " + gson.toJson(details) + "}");
                }
            } else {
                sendJson(ex, 422, "{\"error\": \"Ошибка добавления фильма\", \"details\": \"Данные переданы не в формате JSON\"}");
            }
        } catch (Exception exception) {
            sendJson(ex, 422, " {\n" +
                    "     \"error\": \"Ошибка добавления фильма\",\n" +
                    "   }");
        }
    }

    private void methodDelete(HttpExchange ex, String[] pathList) throws IOException {

        try {
            if (pathList.length == 3) {
                int idValue = Integer.parseInt(pathList[2].trim()); // Пытаемся преобразовать
                HashMap<Integer, Movie> movies = moviesStore.getMovies();
                if (movies.containsKey(idValue)) {
                    movies.remove(idValue);
                    sendJson(ex, 204, "Фильм удален");
                } else {
                    sendJson(ex, 404, "{\n" +
                            "     \"error\": \"Фильм не найден\",\n" +
                            "   }");
                }
            } else {
                sendJson(ex, 400, "{\n" +
                        "     \"error\": \"Ошибка при удалении фильма\",\n" +
                        "   }");
            }
        } catch (NumberFormatException e) {
            sendJson(ex, 400, "{\n" +
                    "     \"error\": \"Некорректный ID\",\n" +
                    "   }");
        } catch (Exception exception) {
            sendJson(ex, 400, "{\n" +
                    "     \"error\": \"Ошибка при удалении фильма\",\n" +
                    "   }");
        }
    }
}
