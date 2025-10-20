package com.enterprise.calendar.exception;

public class GraphAPIException extends RuntimeException {

    public GraphAPIException(String message) {
        super(message);
    }

    public GraphAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
