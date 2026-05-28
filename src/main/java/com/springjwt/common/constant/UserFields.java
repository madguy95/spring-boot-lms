package com.springjwt.common.constant;

public final class UserFields {
    private UserFields() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String PASSWORD = "password";
}
