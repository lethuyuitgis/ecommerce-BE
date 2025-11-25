package com.shopcuathuy.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9]{10,11}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.replaceAll("[^0-9]", "")).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }

    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public static void validatePhone(String phone) {
        if (phone != null && !phone.isEmpty() && !isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone format");
        }
    }

    public static void validatePassword(String password) {
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }
}

