package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.aspect.annotation.CurrentUserToModel;
import com.artyommameev.quester.controller.page.exception.Page_ForbiddenException;
import com.artyommameev.quester.controller.page.exception.Page_NotFoundException;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.entity.gamenode.ChoiceNode;
import com.artyommameev.quester.entity.gamenode.ConditionNode;
import com.artyommameev.quester.entity.gamenode.FlagNode;
import com.artyommameev.quester.entity.gamenode.RoomNode;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.GameService;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

/**
 * A controller for handling a {@link Game} editing page.
 *
 * @author Artyom Mameev
 */
@Controller
@RequestMapping("/games/{gameId}/edit")
public class GameEditorPage {

    private final GameService gameService;
    private final ActualUser actualUser;

    private final String roomIconPath;
    private final String choiceIconPath;
    private final String flagIconPath;
    private final String conditionIconPath;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param gameService       a service that allows to query, interact and save
     *                          {@link Game} objects
     * @param actualUser        the {@link ActualUser} abstraction which
     *                          represents current normal or oAuth2 user.
     * @param roomIconPath      a relative icon url for {@link RoomNode},
     *                          defined in the application.properties
     * @param choiceIconPath    a relative icon url for {@link ChoiceNode},
     *                          defined in the application.properties
     * @param flagIconPath      a relative icon url for {@link FlagNode},
     *                          defined in the application.properties
     * @param conditionIconPath a relative icon url for {@link ConditionNode},
     *                          defined in the application.properties
     * @see GameService
     * @see ActualUser
     */
    public GameEditorPage(GameService gameService, ActualUser actualUser,
                          @Value("${quester.game-editor.room-icon-path}")
                          String roomIconPath,
                          @Value("${quester.game-editor.choice-icon-path}")
                          String choiceIconPath,
                          @Value("${quester.game-editor.flag-icon-path}")
                          String flagIconPath,
                          @Value("${quester.game-editor.condition-icon-path}")
                          String conditionIconPath) {
        this.gameService = gameService;
        this.actualUser = actualUser;
        this.roomIconPath = roomIconPath;
        this.choiceIconPath = choiceIconPath;
        this.flagIconPath = flagIconPath;
        this.conditionIconPath = conditionIconPath;
    }

    /**
     * Handles GET requests to endpoint '/games/{gameId}/edit' and returns
     * a page for editing a certain {@link Game}.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'game' - the {@link Game} that should be edited;<br>
     * 'roomIconUrl' - a relative icon url for a {@link RoomNode};<br>
     * 'choiceIconUrl' - a relative icon url for a {@link ChoiceNode};<br>
     * 'flagIconUrl' - a relative icon url for a {@link FlagNode};<br>
     * 'conditionIconUrl' - a relative icon url for a {@link ConditionNode};<br>
     * 'user' - the current {@link User} object, adds via
     * {@link CurrentUserToModelAspect}.
     *
     * @param gameId  a path variable that represents an id of the
     *                {@link Game} that should be edited.
     * @param request the HTTP servlet request.
     * @param model   the Spring MVC model.
     * @return a page with 'game-editor' template.
     * @throws Page_NotFoundException  if the {@link Game} with the given id
     *                                 is not found.
     * @throws Page_ForbiddenException if the {@link Game} not allows to be
     *                                 edited from the current {@link User}.
     */
    @GetMapping
    @CurrentUserToModel
    public String showGameEditorPage(HttpServletRequest request, Model model,
                                     @PathVariable long gameId) {
        try {
            val game = gameService.getGameForEdit(gameId,
                    actualUser.getCurrentUser());

            val baseUrl = getBaseUrl(request);

            model.addAttribute("game", game);
            model.addAttribute("roomIconUrl", baseUrl + roomIconPath);
            model.addAttribute("choiceIconUrl", baseUrl + choiceIconPath);
            model.addAttribute("flagIconUrl", baseUrl + flagIconPath);
            model.addAttribute("conditionIconUrl", baseUrl +
                    conditionIconPath);

            return "game-editor";
        } catch (GameService.GameNotFoundException e) {
            throw new Page_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Page_ForbiddenException(e);
        }
    }

    private String getBaseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
    }
}