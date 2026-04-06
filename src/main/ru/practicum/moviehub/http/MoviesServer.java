package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private HttpServer server;
    private MoviesStore moviesStore;

    public MoviesServer(MoviesStore newMoviesStore, int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/movies", new MoviesHandler(newMoviesStore));

        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
        moviesStore = newMoviesStore;
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен");
    }

    public MoviesStore getMoviesStore() {
        return moviesStore;
    }
}