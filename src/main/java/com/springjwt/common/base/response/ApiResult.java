package com.springjwt.common.base.response;

public record ApiResult<T>(
        boolean result,
        String message,
        T data,
        Object error
) {
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, "Success", data, null);
    }

    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(false, message, null, null);
    }
}
