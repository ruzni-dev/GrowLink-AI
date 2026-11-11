package com.growlink.backend.exception;

public final class PasswordPolicy {
    private PasswordPolicy() {}

    public static boolean isStrong(String password) {
        return password != null
                && password.length() >= 8
                && password.chars().anyMatch(Character::isUpperCase)
                && password.chars().anyMatch(Character::isLowerCase)
                && password.chars().anyMatch(Character::isDigit);
    }
}
