package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.aspect.annotation.CurrentUserToModel;
import com.artyommameev.quester.controller.page.exception.Page_ForbiddenException;
import com.artyommameev.quester.controller.page.exception.Page_NotFoundException;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.Review;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.GameService;
import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A controller for handling a page for viewing a certain {@link Game} details.
 *
 * @author Artyom Mameev
 */
@Controller
public class GameInfoPage {

    private final GameService gameService;
    private final ActualUser actualUser;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param gameService a service that allows to query, interact and save
     *                    {@link Game} objects
     * @param actualUser  the {@link ActualUser} abstraction which represents
     *                    current normal or oAuth2 user.
     * @see GameService
     * @see ActualUser
     */
    public GameInfoPage(GameService gameService, ActualUser actualUser) {
        this.gameService = gameService;
        this.actualUser = actualUser;
    }

    /**
     * Handles GET requests to endpoint '/games/{gameId}' and returns
     * a page for viewing a certain {@link Game} details.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'review' - an integer from 0 to 5 which represents the rating of the game
     * from the current user. If the current user has not rated the game,
     * the value is 0;<br>
     * 'game' - the {@link Game} which details should be viewed;<br>
     * 'user' - the current {@link User} object, adds via
     * {@link CurrentUserToModelAspect}.
     *
     * @param gameId a path variable that represents an id of the
     *               {@link Game} which details should be viewed.
     * @param model  the Spring MVC model.
     * @return a page with 'game-info' template, or if current {@link User} is
     * logged in and tries to view details of the unpublished {@link Game}
     * that the {@link User} can edit, redirects to the {@link GameEditorPage}.
     * @throws Page_NotFoundException  if the {@link Game} is not found.
     * @throws Page_ForbiddenException if the {@link Game} not allows to be
     *                                 viewed from the current {@link User}.
     * @see GameEditorPage
     */
    @RequestMapping("/games/{gameId}")
    @CurrentUserToModel
    @SuppressWarnings("SpringMVCViewInspection") /*redirect address was treated by IDE as
                                a view name*/
    public String showGameInfoPage(@PathVariable long gameId, Model model) {
        try {
            val game = gameService.getGameForView(gameId,
                    actualUser.getCurrentUser());

            /*if the game is not published and can be modified by the current
            user, redirect to the game editing page*/
            if (actualUser.isLoggedIn() &&
                    (!game.isPublished() && game.canBeModifiedFrom(
                            actualUser.getCurrentUser()))) {
                return "redirect:/games/" + gameId + "/edit";
            }

            Review userReview = null;

            if (actualUser.isLoggedIn()) {
                userReview = game.getReviewFor(actualUser.getCurrentUser());
            }

            model.addAttribute("game", game);
            model.addAttribute("review", userReview != null ?
                    userReview.getRating() : 0);

            return "game-info";
        } catch (GameService.GameNotFoundException e) {
            throw new Page_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Page_ForbiddenException(e);
        }
    }
}
