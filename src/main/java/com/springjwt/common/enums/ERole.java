package com.springjwt.common.enums;

public enum ERole {
    ROLE_USER,
    ROLE_MODERATOR,
    ROLE_ADMIN;

    public String getDescription() {
        return switch (this) {
            case ROLE_USER -> "Regular User";
            case ROLE_MODERATOR -> "Moderator";
            case ROLE_ADMIN -> "Administrator";
        };
    }

    public int getPriority() {
        return switch (this) {
            case ROLE_USER -> 1;
            case ROLE_MODERATOR -> 2;
            case ROLE_ADMIN -> 3;
        };
    }
}

