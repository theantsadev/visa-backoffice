package com.example.demo.controller;

import java.time.LocalDateTime;

public class ApiErrorResponse {

    private final String code;
    private final String message;
    private final String details;
    private final LocalDateTime timestamp;

    public ApiErrorResponse(String code, String message, String details, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
