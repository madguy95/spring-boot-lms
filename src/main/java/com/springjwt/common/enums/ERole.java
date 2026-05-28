package com.springjwt.common.enums;

public enum ERole {
    ROLE_PARENT,
    ROLE_TEACHER,
    ROLE_ADMIN;

    public String getDescription() {
        return switch (this) {
            case ROLE_PARENT -> "Parent";
            case ROLE_TEACHER -> "Teacher";
            case ROLE_ADMIN -> "Administrator";
        };
    }

    public int getPriority() {
        return switch (this) {
            case ROLE_PARENT -> 1;
            case ROLE_TEACHER -> 2;
            case ROLE_ADMIN -> 3;
        };
    }
}

