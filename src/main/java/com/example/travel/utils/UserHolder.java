package com.example.travel.utils;

import com.example.travel.dto.UserDTO;

public final class UserHolder {

    private static final ThreadLocal<UserDTO> THREAD_LOCAL = new ThreadLocal<>();

    private UserHolder() {
    }

    public static void saveUser(UserDTO user) {
        THREAD_LOCAL.set(user);
    }

    public static UserDTO getUser() {
        return THREAD_LOCAL.get();
    }

    public static void removeUser() {
        THREAD_LOCAL.remove();
    }
}
