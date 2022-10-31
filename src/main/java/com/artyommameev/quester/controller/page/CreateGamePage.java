package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.aspect.annotation.CurrentUserToModel;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A controller for handling a {@link Game} creating page.
 *
 * @author Artyom Mameev
 */
@Controller
@RequestMapping("/games/create")
public class CreateGamePage {

    /**
     * Handles GET requests to endpoint '/games/create' and returns
     * a {@link Game} creating page.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'user' - the current {@link User} object, adds via
     * {@link CurrentUserToModelAspect}.
     *
     * @param model the Spring MVC model.
     * @return a page with 'create-game' template.
     */
    @GetMapping
    @CurrentUserToModel
    public String showCreateGamePage(Model model) {
        return "create-game";
    }
}
