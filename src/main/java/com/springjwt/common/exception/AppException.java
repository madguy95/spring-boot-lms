package com.springjwt.common.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.io.IOException;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AppException extends RuntimeException {

    protected HttpStatusCode statusCode;

    protected String message;

    protected String messageDesc;

    @JsonIgnore
    protected Object[] args;

    public AppException(String message) {
        this.message = message;
        this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public AppException(String message, HttpStatus statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public AppException(String couldNotInitializeStorageLocation, Throwable e) {
        this.message = e.getMessage();
        this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}