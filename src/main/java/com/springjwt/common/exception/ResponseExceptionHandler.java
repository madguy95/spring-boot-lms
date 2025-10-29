package com.springjwt.common.exception;

import com.springjwt.common.base.response.ApiResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.springjwt.common.constant.MsgConstants.UNEXPECTED_ERROR;

/**
 * Global exception handler for the application
 * Centralizes all exception handling and message translation
 *
 * @author Spring JWT Team
 * @version 1.0
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    /**
     * Get translated message from message source with fallback
     *
     * @param messageCode The message code to translate
     * @param args Optional arguments for parameterized messages
     * @return Translated message or message code as fallback
     */
    private String getTranslatedMessage(String messageCode, Object... args) {
        if (!StringUtils.hasText(messageCode)) {
            log.warn("Empty message code provided, using default error message");
            return getTranslatedMessage(UNEXPECTED_ERROR);
        }

        try {
            return messageSource.getMessage(messageCode, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            log.warn("Message code '{}' not found in properties file, using code as fallback", messageCode);
            return messageCode;
        }
    }

    /**
     * Build standard error response with translated message
     *
     * @param messageCode The message code
     * @param status HTTP status
     * @param args Optional message arguments
     * @return ResponseEntity with error response
     */
    private ResponseEntity<Object> buildErrorResponse(String messageCode, HttpStatus status, Object... args) {
        String message = getTranslatedMessage(messageCode, args);
        ApiResult<Object> response = new ApiResult<>(false, message, messageCode, null);
        log.debug("Building error response: status={}, messageCode={}", status, messageCode);
        return new ResponseEntity<>(response, status);
    }

    /**
     * Build validation error response
     *
     * @param errors List of validation errors
     * @param status HTTP status
     * @return ResponseEntity with validation errors
     */
    private ResponseEntity<Object> buildValidationErrorResponse(List<ValidationError> errors, HttpStatus status) {
        ApiResult<Object> response = new ApiResult<>(false, null, null, errors);
        log.debug("Building validation error response: {} errors, status={}", errors.size(), status);
        return new ResponseEntity<>(response, status);
    }

    // ==================== Authentication & Authorization Exceptions ====================

    /**
     * Handle BadCredentialsException - Invalid username/password
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        String messageCode = ex.getMessage();
        log.debug("BadCredentialsException: messageCode={}", messageCode);
        return buildErrorResponse(messageCode, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle AccessDeniedException - Forbidden access
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        log.debug("AccessDeniedException: {}", ex.getMessage());
        return buildErrorResponse("auth.forbidden", HttpStatus.FORBIDDEN);
    }

    // ==================== Validation Exceptions ====================

    /**
     * Handle validation errors from @Valid/@Validated on request body
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<ValidationError> validationErrors = new ArrayList<>();

        // Field errors
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String messageCode = fieldError.getDefaultMessage();
            String translatedMessage = getTranslatedMessage(messageCode);
            validationErrors.add(new ValidationError(fieldError.getField(), translatedMessage));
        });

        // Global errors
        ex.getBindingResult().getGlobalErrors().forEach(globalError -> {
            String messageCode = globalError.getDefaultMessage();
            String translatedMessage = getTranslatedMessage(messageCode);
            validationErrors.add(new ValidationError(globalError.getObjectName(), translatedMessage));
        });

        log.debug("Method argument validation failed: {} errors", validationErrors.size());
        return buildValidationErrorResponse(validationErrors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violations from @Validated on method parameters
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<ValidationError> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String fieldName = getFieldNameFromPath(violation);
                    String messageCode = violation.getMessage();
                    String translatedMessage = getTranslatedMessage(messageCode);
                    return new ValidationError(fieldName, translatedMessage);
                })
                .collect(Collectors.toList());

        log.debug("Constraint violation: {} errors", validationErrors.size());
        return buildValidationErrorResponse(validationErrors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Extract field name from constraint violation property path
     */
    private String getFieldNameFromPath(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        String[] parts = path.split("\\.");
        return parts.length > 0 ? parts[parts.length - 1] : path;
    }

    /**
     * Handle ValidationException - Custom validation errors
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        ValidationError error = ex.getError();
        String messageCode = error.errorMessage();
        String translatedMessage = getTranslatedMessage(messageCode);

        List<ValidationError> validationErrors = List.of(
                new ValidationError(error.paramName(), translatedMessage)
        );

        log.debug("ValidationException: field={}, messageCode={}", error.paramName(), messageCode);
        return buildValidationErrorResponse(validationErrors, HttpStatus.BAD_REQUEST);
    }

    // ==================== Spring MVC Exceptions ====================

    /**
     * Handle missing request parameters
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        ApiResult<Object> response = new ApiResult<>(false, message, "error.missing.parameter", null);
        log.debug("Missing request parameter: {}", ex.getParameterName());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ApiResult<Object> response = new ApiResult<>(false, message, "error.type.mismatch", null);
        log.debug("Method argument type mismatch: parameter={}, requiredType={}",
                ex.getName(), ex.getRequiredType());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle HTTP message not readable (malformed JSON)
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.debug("HTTP message not readable: {}", ex.getMessage());
        return buildErrorResponse("error.malformed.request", HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle unsupported HTTP method
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String message = String.format("Method '%s' is not supported for this endpoint", ex.getMethod());
        ApiResult<Object> response = new ApiResult<>(false, message, "error.method.not.supported", null);
        log.debug("HTTP method not supported: {}", ex.getMethod());
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle unsupported media type
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.debug("HTTP media type not supported: {}", ex.getContentType());
        return buildErrorResponse("error.media.type.not.supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handle no handler found (404)
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.debug("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return buildErrorResponse("error.not.found", HttpStatus.NOT_FOUND);
    }

    // ==================== Custom Application Exceptions ====================

    /**
     * Handle AppException - Custom application exceptions with status code
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Object> handleAppException(AppException ex) {
        String messageCode = ex.getMessage();
        Object[] args = ex.getArgs();
        String translatedMessage = getTranslatedMessage(messageCode, args);

        // Use standard response format for consistency
        ApiResult<Object> response = new ApiResult<>(false, translatedMessage, messageCode, null);
        log.debug("AppException: messageCode={}, status={}", messageCode, ex.getStatusCode());
        return new ResponseEntity<>(response, ex.getStatusCode() != null ? ex.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle BadRequestException - Bad request errors
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {
        String messageCode = ex.getMessage();
        log.debug("BadRequestException: messageCode={}", messageCode);
        return buildErrorResponse(messageCode, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException - Invalid arguments (business logic)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        String messageCode = ex.getMessage();
        log.warn("IllegalArgumentException: {}", messageCode);
        return buildErrorResponse(messageCode, HttpStatus.BAD_REQUEST);
    }

    // ==================== Generic Exception Handlers ====================

    /**
     * Handle RuntimeException - Catch specific runtime exceptions
     * Note: This should be used carefully - only for known business exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        String messageCode = ex.getMessage();

        // Log full stack trace for debugging
        log.error("RuntimeException caught: type={}, messageCode={}",
                ex.getClass().getSimpleName(), messageCode, ex);

        // For security, don't expose internal error details in production
        return buildErrorResponse(messageCode != null ? messageCode : UNEXPECTED_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions - Last resort handler
     * This catches any exception not handled by specific handlers above
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        log.error("Unexpected exception caught: type={}, message={}",
                ex.getClass().getSimpleName(), ex.getMessage(), ex);

        // Don't expose internal error details
        return buildErrorResponse(UNEXPECTED_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
