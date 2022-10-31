package com.artyommameev.quester.controller.api.exception;

import com.artyommameev.quester.controller.ApiExceptionHandler;

/**
 * The exception that is used at Spring MVC controllers level when a
 * security error is detected in client request to produce a simple
 * HTTP response with FORBIDDEN status.
 * <p>
 * The exception is handled by {@link ApiExceptionHandler}.
 *
 * @author Artyom Mameev
 */
public class Api_ForbiddenException extends RuntimeException {

    /**
     * The exception constructor.
     *
     * @param cause the cause of the exception. Text of the root cause exception
     *              will be used in the HTTP response.
     */
    public Api_ForbiddenException(Throwable cause) {
        super(cause);
    }
}
