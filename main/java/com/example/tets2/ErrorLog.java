package com.example.tets2;

public class ErrorLog {
    private int id;
    private int code;
    private String message;

    public ErrorLog() {

    }

    public ErrorLog(int id, int code, String message) {
        this.id = id;
        this.code = code;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
