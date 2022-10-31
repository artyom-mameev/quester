package com.artyommameev.quester.controller.api.exception;

import com.artyommameev.quester.controller.ApiExceptionHandler;

/**
 * An exception that is used at Spring MVC controllers level when a
 * syntax error is detected in client request to produce a simple
 * HTTP response with BAD REQUEST status.
 * <p>
 * The exception is handled by {@link ApiExceptionHandler}.
 *
 * @author Artyom Mameev
 */
public class Api_BadRequestException extends RuntimeException {

    /**
     * The exception constructor.
     *
     * @param cause the cause of the exception. Text of the root cause exception
     *              will be used in the HTTP response.
     */
    public Api_BadRequestException(Throwable cause) {
        super(cause);
    }
}
