package com.springjwt.common.exception;

public record ValidationError(
        String paramName,
        String errorMessage
) {
}
