package ru.practicum.moviehub.api;

public class ResultResponse {
    private int result;
    private int id;
    private ErrorResponse error;

    public ResultResponse(int result, int id, ErrorResponse error) {
        this.result = result;
        this.id = id;
        this.error = error;
    }

    public ResultResponse(int result, int id) {
        this.result = result;
        this.id = id;
    }

    public ResultResponse(int result, ErrorResponse error) {
        this.result = result;
        this.error = error;
    }

    public int getResult() {
        return result;
    }

    public int getId() {
        return id;
    }

    public ErrorResponse getError() {
        return error;
    }
}
