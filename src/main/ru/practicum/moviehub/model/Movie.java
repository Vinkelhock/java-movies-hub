package ru.practicum.moviehub.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Movie {
    private int id;
    private String title;
    private int year;
    private static int currentId = 1;

    public Movie(String title, int year) {
        this.title = title;
        this.year = year;
        this.id = currentId;
        currentId = currentId + 1;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public static void setId(Movie movie) {
        movie.id = currentId;
        currentId = currentId + 1;
    }

    public static List<String> checkMovie(Movie movie) {
        List<String> details = new ArrayList<>();
        String title = movie.getTitle();
        if (title == null) {
            details.add("Название фильма не может быть пустым");
        } else if (title.length() > 100) {
            details.add("Название фильма не может быть длиннее 100 символов");
        }
        if (movie.getYear() < 1888 || movie.getYear() > (LocalDate.now().getYear() + 1)) {
            details.add("Год фильма должен быть между 1888 и " + (LocalDate.now().getYear() + 1));
        }
        return details;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", year=" + year +
                '}';
    }
}