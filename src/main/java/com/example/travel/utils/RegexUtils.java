package com.example.travel.utils;

import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

public final class RegexUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private RegexUtils() {
    }

    public static boolean isPhoneInvalid(String phone) {
        return !StringUtils.hasText(phone) || !PHONE_PATTERN.matcher(phone).matches();
    }
}
