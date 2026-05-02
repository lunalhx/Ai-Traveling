package com.example.travel.utils;

import java.util.UUID;

public final class IdUtils {

    private IdUtils() {
    }

    public static String simpleUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
