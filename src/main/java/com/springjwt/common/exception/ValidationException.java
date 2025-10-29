package com.springjwt.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ValidationException extends RuntimeException {

    private ValidationError error;

    public ValidationException() {
        super("validation.failed");
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String paramName, String message) {
        super();
        this.error = new ValidationError(paramName, message);
    }
}
