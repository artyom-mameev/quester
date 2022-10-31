package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.aspect.annotation.CurrentUserToModel;
import com.artyommameev.quester.entity.User;
import lombok.val;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * A controller for handling error pages.
 *
 * @author Artyom Mameev
 */
@Controller
public class ErrorPage implements ErrorController {

    /**
     * Handles GET requests to endpoint '/error' and returns a correct error
     * page depending on the error code in HTTP servlet request.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'user' - the current {@link User} object, adds via
     * {@link CurrentUserToModelAspect}.
     *
     * @param request the HTTP servlet request.
     * @param model   the Spring MVC model.
     * @return a page with error page template depending on the error code
     * in the HTTP servlet request. For codes other than
     * {@link HttpStatus#BAD_REQUEST}, {@link HttpStatus#NOT_FOUND},
     * {@link HttpStatus#FORBIDDEN} and {@link HttpStatus#INTERNAL_SERVER_ERROR},
     * returns general error page with template 'error/general'.
     */
    @RequestMapping("/error")
    @CurrentUserToModel
    public String showErrorPage(HttpServletRequest request, Model model) {
        val status =
                request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status == null) {
            return "error/general";
        }

        int statusCode = Integer.parseInt(status.toString());

        if (statusCode == HttpStatus.BAD_REQUEST.value()) {
            return "error/400";
        }
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return "error/403";
        }
        if (statusCode == HttpStatus.FORBIDDEN.value()) {
            return "error/404";
        }
        if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return "error/500";
        }

        return "error/general";
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}