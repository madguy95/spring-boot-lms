package com.springjwt.common.base.response;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ResponseFactory {

    private static MessageSource messageSource;

    public ResponseFactory(MessageSource messageSource) {
        ResponseFactory.messageSource = messageSource;
    }

    public static <T> ResponseEntity<ApiResult<T>> success(T data) {
        return new ResponseEntity<>(ApiResult.success(data), HttpStatus.OK);
    }

    public static <T> ResponseEntity<ApiResult<T>> error(String message) {
        return new ResponseEntity<>(ApiResult.error(message), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static <T> ResponseEntity<ApiResult<String>> error(HttpStatus status, Object... args) {
        String messageCode = null;
        String message = null;
        Object[] messageArgs = {};

        if (args.length >= 1 && args[0] instanceof String code) {
            messageCode = code;
            messageArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        if (!StringUtils.isEmpty(messageCode)) {
            message = messageSource.getMessage(messageCode, messageArgs, messageCode, LocaleContextHolder.getLocale());
        }
        ApiResult<String> response = new ApiResult<>(false, message, messageCode, null);
        return new ResponseEntity<>(response, status);
    }

    public static <T> ResponseEntity<PagedResult<T>> pagedResponse(
            List<T> data, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        PagedResult<T> pagedResponse = new PagedResult<>(HttpStatus.OK, data, page, size, totalElements, totalPages);
        return new ResponseEntity<>(pagedResponse, HttpStatus.OK);
    }

    public static <T> ResponseEntity<PagedResult<T>> success(
            Page<T> data) {
        int totalPages = (int) Math.ceil((double) data.getTotalElements() / data.getSize());
        PagedResult<T> pagedResponse = new PagedResult<>(HttpStatus.OK, data.getContent(),
                data.getPageable().getPageNumber(), data.getSize(), data.getTotalElements(), totalPages);
        return new ResponseEntity<>(pagedResponse, HttpStatus.OK);
    }
}
