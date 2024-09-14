package com.pmp.restful_web_service.exception;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.pmp.restful_web_service.exception.errors.UserNotFoundException;
import com.pmp.restful_web_service.exception.model.Error;
import com.pmp.restful_web_service.exception.model.ValidationError;
import com.pmp.restful_web_service.exception.model.ValidationField;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Error> handleAllException(Exception ex, WebRequest request) throws Exception {
        return new ResponseEntity<Error>(getError(ex, request), getErrorCode(ex));
    }

    /**
     * The below method will be redundant since the handleAllException method
     * provides a generic implementation to return the appropriate status code
     * using reflection.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public final ResponseEntity<Error> handleUserNotFoundExceptionException(Exception ex, WebRequest request)
            throws Exception {
        return new ResponseEntity<Error>(getError(ex, request),
                HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        return new ResponseEntity<Object>(getValidationErrorForMethodArgumentNotValidException(ex), status);
    }

    // #region Private Methods

    private static Error getError(Exception ex, WebRequest request) {
        return new Error(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
    }

    private static HttpStatus getErrorCode(Exception ex) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        return responseStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR : responseStatus.code();
    }

    private static ValidationError getValidationErrorForMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        List<ValidationField> list = new ArrayList<ValidationField>();

        for (ObjectError error : ex.getAllErrors()) {

            var fieldName = error instanceof FieldError ? ((FieldError) error).getField() : "unknown";

            list.add(new ValidationField(fieldName, error.getDefaultMessage()));
        }

        return getValidationErrors(list);
    }

    private static ValidationError getValidationErrors(List<ValidationField> validationErrors) {
        return new ValidationError(LocalDateTime.now(), validationErrors);
    }

    // #endregion

}