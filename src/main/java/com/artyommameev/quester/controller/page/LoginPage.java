package com.artyommameev.quester.controller.page;

import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * A controller for handling a login page.
 *
 * @author Artyom Mameev
 */
@Controller
public class LoginPage {

    /**
     * Handles GET requests to endpoint '/login' and returns a login page.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'oauth2Error' - a message that describes oauth2 error, if any oauth2 error
     * is occurred.
     *
     * @param request the HTTP servlet request.
     * @param model   the Spring MVC model.
     * @return a page with 'login' template.
     */
    @RequestMapping("/login")
    public String showLoginPage(HttpServletRequest request, Model model) {
        val message = (String) request.getSession().getAttribute(
                "error.message");

        if (message == null) {
            return "login";
        }

        request.getSession().removeAttribute("error.message");

        model.addAttribute("oauth2Error", message);

        return "login";
    }
}
