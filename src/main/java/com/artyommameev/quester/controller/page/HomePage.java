package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.aspect.annotation.CurrentUserToModel;
import com.artyommameev.quester.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A controller for handling a home page.
 *
 * @author Artyom Mameev
 */
@Controller
public class HomePage {

    /**
     * Handles GET requests to endpoint '/' and returns a home page.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'user' - the current {@link User} object, adds via
     * {@link CurrentUserToModelAspect}.
     *
     * @param model the Spring MVC model.
     * @return a page with 'home' template.
     */
    @RequestMapping("/")
    @CurrentUserToModel
    public String showHomePage(Model model) {
        return "home";
    }
}
