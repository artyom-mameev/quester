package com.artyommameev.quester.controller;

import com.artyommameev.quester.controller.page.HomePage;
import com.artyommameev.quester.controller.page.exception.Page_BadRequestException;
import com.artyommameev.quester.entity.User;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A controller advice for handling webpage exceptions.
 *
 * @author Artyom Mameev
 */
@ControllerAdvice
public class PageExceptionHandler {

    /**
     * Handles {@link DisabledException}.
     * <p>
     * Sets response status to {@link HttpStatus#FORBIDDEN}, log outs current
     * {@link User} and if HTTP request method is 'GET', redirects to the
     * {@link HomePage}.
     *
     * @param request  the HTTP servlet request.
     * @param response the HTTP servlet response.
     */
    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public void logOutDisabledUser(HttpServletRequest request,
                                   HttpServletResponse response)
            throws IOException {
        new SecurityContextLogoutHandler().logout(request, null,
                null);

        if (request.getMethod().equals("GET")) {
            response.sendRedirect("/");
        }
    }

    /**
     * Handles {@link ConversionFailedException}.
     * <p>
     * Just throws new {@link Page_BadRequestException} with current exception
     * as a cause.
     */
    @ExceptionHandler(ConversionFailedException.class)
    public void handleConversionFailedException(RuntimeException ex) {
        throw new Page_BadRequestException(ex);
    }
}
