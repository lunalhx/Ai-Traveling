package com.example.travel.common;

public enum ErrorCode {

    SUCCESS(0, "success"),
    PARAMS_ERROR(40000, "request parameter error"),
    NOT_LOGIN_ERROR(40100, "not logged in"),
    NO_AUTH_ERROR(40300, "no permission"),
    NOT_FOUND_ERROR(40400, "resource not found"),
    TOO_MANY_REQUESTS(42900, "too many requests"),
    SYSTEM_ERROR(50000, "system error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
