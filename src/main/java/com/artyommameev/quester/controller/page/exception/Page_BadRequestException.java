package com.artyommameev.quester.controller.page.exception;

import com.artyommameev.quester.controller.PageExceptionHandler;
import com.artyommameev.quester.controller.page.ErrorPage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The exception that is used at Spring MVC controllers level when a
 * syntax error is detected in client request to produce a redirect
 * to the BAD REQUEST {@link ErrorPage}.
 * <p>
 * The exception is handled by {@link PageExceptionHandler}.
 *
 * @author Artyom Mameev
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class Page_BadRequestException extends RuntimeException {

    /**
     * The exception constructor.
     *
     * @param cause the cause of the exception.
     */
    public Page_BadRequestException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}