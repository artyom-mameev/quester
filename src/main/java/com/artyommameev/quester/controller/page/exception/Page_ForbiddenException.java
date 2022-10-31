package com.artyommameev.quester.controller.page.exception;

import com.artyommameev.quester.controller.PageExceptionHandler;
import com.artyommameev.quester.controller.page.ErrorPage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The exception that is used at Spring MVC controllers level when a
 * security error is detected in client request to produce a redirect
 * to the FORBIDDEN {@link ErrorPage}.
 * <p>
 * The exception is handled by {@link PageExceptionHandler}.
 *
 * @author Artyom Mameev
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class Page_ForbiddenException extends RuntimeException {

    /**
     * The exception constructor.
     *
     * @param cause the cause of the exception.
     */
    public Page_ForbiddenException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
