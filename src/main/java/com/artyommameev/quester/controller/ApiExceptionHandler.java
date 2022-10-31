package com.artyommameev.quester.controller;

import com.artyommameev.quester.controller.api.exception.Api_BadRequestException;
import com.artyommameev.quester.controller.api.exception.Api_ForbiddenException;
import com.artyommameev.quester.controller.api.exception.Api_NotFoundException;
import com.artyommameev.quester.validation.ValidationResponse;
import lombok.val;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * A controller advice for handling API exceptions.
 *
 * @author Artyom Mameev
 * @see Api_BadRequestException
 * @see Api_NotFoundException
 * @see Api_ForbiddenException
 */
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles {@link Api_BadRequestException}.
     *
     * @param ex      the {@link Api_BadRequestException} that should be handled.
     * @param request the web request.
     * @return a {@link ResponseEntity} with {@link HttpStatus#BAD_REQUEST}
     * HTTP status and root cause message of the exception.
     */
    @ExceptionHandler(value = {Api_BadRequestException.class})
    protected ResponseEntity<Object> handleApiBadRequestException(
            RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ExceptionUtils.getRootCauseMessage(ex),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles {@link Api_NotFoundException}.
     *
     * @param ex      the {@link Api_NotFoundException} that should be handled.
     * @param request the web request.
     * @return a {@link ResponseEntity} with {@link HttpStatus#NOT_FOUND}
     * HTTP status and root cause message of the exception.
     */
    @ExceptionHandler(value = {Api_NotFoundException.class})
    protected ResponseEntity<Object> handleApiNotFoundException(
            RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ExceptionUtils.getRootCauseMessage(ex),
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    /**
     * Handles {@link Api_ForbiddenException}.
     *
     * @param ex      the {@link Api_ForbiddenException} that should be handled.
     * @param request the web request.
     * @return a {@link ResponseEntity} with {@link HttpStatus#FORBIDDEN}
     * HTTP status and root cause message of the exception.
     * @see Api_ForbiddenException
     */
    @ExceptionHandler(value = {Api_ForbiddenException.class})
    protected ResponseEntity<Object> handleApiForbiddenException(
            RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ExceptionUtils.getRootCauseMessage(ex),
                new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    /**
     * Handles a validation exception.
     *
     * @param ex      the {@link MethodArgumentNotValidException} that should
     *                be handled.
     * @param headers the HTTP headers.
     * @param status  the HTTP status.
     * @param request the web request.
     * @return a {@link ResponseEntity} with {@link HttpStatus#BAD_REQUEST}
     * HTTP status and {@link ValidationResponse} object that represents results
     * of the validation.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        val validationResponse = new ValidationResponse(true);

        validationResponse.setFieldErrors(ex.getBindingResult().getAllErrors());

        return new ResponseEntity<>(validationResponse, HttpStatus.BAD_REQUEST);
    }
}
