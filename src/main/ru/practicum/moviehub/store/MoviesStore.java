package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MoviesStore {
    private final HashMap<Integer, Movie> movies;

    public MoviesStore() {
        movies = new HashMap<Integer, Movie>();
    }

    public int addMovie(Movie newMovie) {
        movies.put(newMovie.getId(), newMovie);
        return newMovie.getId();
    }

    public Optional<Movie> getMovie(int id) {
        return Optional.ofNullable(movies.get(id));
    }

    public List<Movie> getMoviesByYear(int year) {
        List<Movie> matchingMovies = new ArrayList<>();
        for (Movie movie : movies.values()) {
            if (movie.getYear() == year) {
                matchingMovies.add(movie);
            }
        }
        return matchingMovies;
    }

    public void clearMovies() {
        movies.clear();
    }

    public HashMap<Integer, Movie> getMovies() {
        return movies;
    }

    public void removeMovie(int id) {
        movies.remove(id);
    }
}