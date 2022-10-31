package com.artyommameev.quester.controller.api.exception;

import com.artyommameev.quester.controller.ApiExceptionHandler;

/**
 * The exception that is used at Spring MVC controllers level when a
 * not found error is detected in client request to produce a simple
 * HTTP response with NOT FOUND status.
 * <p>
 * The exception is handled by {@link ApiExceptionHandler}.
 *
 * @author Artyom Mameev
 */
public class Api_NotFoundException extends RuntimeException {

    /**
     * The exception constructor.
     *
     * @param cause the cause of the exception. Text of the root cause exception
     *              will be used in the HTTP response.
     */
    public Api_NotFoundException(Throwable cause) {
        super(cause);
    }
}