package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.aspect.annotation.CurrentUserToModel;
import com.artyommameev.quester.controller.page.exception.Page_ForbiddenException;
import com.artyommameev.quester.controller.page.exception.Page_NotFoundException;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.GameService;
import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A controller for handling a page with {@link Game} player.
 *
 * @author Artyom Mameev
 */
@Controller
public class PlayPage {

    private final GameService gameService;
    private final ActualUser actualUser;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param gameService a service that allows to query, interact and save
     *                    {@link Game} objects.
     * @param actualUser  the {@link ActualUser} abstraction which represents
     *                    current normal or oAuth2 user.
     * @see GameService
     * @see ActualUser
     */
    public PlayPage(GameService gameService, ActualUser actualUser) {
        this.gameService = gameService;
        this.actualUser = actualUser;
    }

    /**
     * Handles GET requests to endpoint '/games/{gameId}/play' and returns
     * a page with {@link Game} player.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'game' - the {@link Game} which should be played;<br>
     * 'user' - the current {@link User} object, adds via
     * {@link CurrentUserToModelAspect}.
     *
     * @param model  the Spring MVC model.
     * @param gameId a path variable that represents an id of the
     *               {@link Game} that should be played.
     * @return a page with 'play' template.
     * @throws Page_NotFoundException  if the {@link Game} is not found.
     * @throws Page_ForbiddenException if the {@link Game} not allows to be
     *                                 played from the current {@link User}.
     */
    @RequestMapping("/games/{gameId}/play")
    @CurrentUserToModel
    public String showGamePlayerPage(Model model,
                                     @PathVariable long gameId) {
        try {
            val game = gameService.getGameForView(gameId,
                    actualUser.getCurrentUser());

            model.addAttribute("game", game);

            return "play";
        } catch (GameService.GameNotFoundException e) {
            throw new Page_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Page_ForbiddenException(e);
        }
    }
}
