package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.aspect.annotation.CurrentUserToModel;
import com.artyommameev.quester.controller.page.exception.Page_BadRequestException;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.GameService;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

import static com.artyommameev.quester.QuesterApplication.PAGE_SIZE;

/**
 * A controller for handling a pages for viewing a list of {@link Game}s.
 *
 * @author Artyom Mameev
 */
@Controller
public class GamesPage {

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
    public GamesPage(GameService gameService, ActualUser actualUser) {
        this.gameService = gameService;
        this.actualUser = actualUser;
    }

    /**
     * Handles GET requests to endpoint '/games' and returns a page for
     * viewing a list of published {@link Game}s.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'mode' - a value of the {@link FilterMode} enumeration that indicates
     * current game filter mode. Equals to {@link FilterMode#ALL} if 'userId'
     * path variable is missing (all games will be shown), or
     * {@link FilterMode#CREATED_BY_USER} if 'userId' path variable is present
     * (only specific {@link Game}s from a specific {@link User} will be shown);<br>
     * 'games' - a list with {@link Game} objects that represents portion of
     * {@link Game}s saved in the database (when 'userId' path variable is
     * missing) or portion of games created by certain {@link User} (when
     * 'userId' path variable is present), selected according to the 'page'
     * and 'sort' request parameters;<br>
     * 'currentPage' - a number of the current page. Always equals to the
     * 'page' request parameter;<br>
     * 'totalPages' - a number of pages into which the {@link Game}s
     * are divided;<br>
     * 'user' - the current {@link User} object, adds via
     * {@link CurrentUserToModelAspect}.
     * <p>
     * The page size is equal to the {@link QuesterApplication#PAGE_SIZE}
     * constant.
     *
     * @param model       the Spring MVC model.
     * @param currentPage a request parameter which represents
     *                    the current page number, cannot be missed.
     * @param sortingMode a request parameter which represents
     *                    a sorting mode of the {@link Game}s, cannot be missed.
     * @param userId      a path variable that represents an id of the
     *                    {@link User} whose {@link Game}s should be shown
     *                    (if missed, all {@link Game}s will be shown instead).
     * @return a page with 'games' template.
     * @throws Page_BadRequestException if the page value is < 0.
     */
    @RequestMapping("/games")
    @CurrentUserToModel
    public String showGamesPage(Model model,
                                @RequestParam(name = "page") int currentPage,
                                @RequestParam(name = "sort")
                                        GameService.SortingMode sortingMode,
                                @RequestParam(required = false, name = "user")
                                        Long userId) {
        try {
            Page<Game> gamesPage;
            FilterMode filterMode;

            if (userId == null) {
                gamesPage = gameService.getPublishedGamesPage(
                        currentPage - 1, PAGE_SIZE, sortingMode);
                filterMode = FilterMode.ALL;
            } else {
                gamesPage = gameService.getUserPublishedGamesPage(
                        currentPage - 1, PAGE_SIZE, sortingMode,
                        userId);
                filterMode = FilterMode.CREATED_BY_USER;
            }

            setUpGamesPage(gamesPage, filterMode, currentPage, model);

            return "games";
        } catch (GameService.IllegalPageValueException e) {
            throw new Page_BadRequestException(e);
        }
    }

    /**
     * Handles GET requests to endpoint '/favorites' and returns
     * a page for viewing a list of {@link Game}s favorited by the current
     * {@link User}.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'mode' - a value of the {@link FilterMode} enumeration that indicates
     * the current game filter mode. Always equals to
     * {@link FilterMode#FAVORITED_BY_USER};<br>
     * 'games' - a list of the {@link Game} objects that represents the portion
     * of the current {@link User} favorites, selected according to the 'page'
     * and 'sort' request parameters;<br>
     * 'currentPage' - a number of the current page. Always equals to
     * the 'page' request parameter;<br>
     * 'totalPages' - a number of pages into which the {@link Game}s are
     * divided;<br>
     * 'user' - the current {@link User} object, adds via
     * {@link CurrentUserToModelAspect}.
     * <p>
     * The page size is equal to the {@link QuesterApplication#PAGE_SIZE}
     * constant.
     *
     * @param model       the Spring MVC model.
     * @param currentPage a request parameter which represents
     *                    a current page number, cannot be missed.
     * @param sortingMode a request parameter which represents
     *                    a sorting mode of the {@link Game}s, cannot be missed.
     * @return a page with 'games' template.
     * @throws Page_BadRequestException if the page value is < 0.
     */
    @RequestMapping("/favorites")
    @CurrentUserToModel
    public String showCurrentUserFavoritedGamesPage(Model model,
                                                    @RequestParam(name = "page")
                                                            int currentPage,
                                                    @RequestParam(name = "sort")
                                                            GameService.SortingMode
                                                            sortingMode) {
        try {
            val gamesPage = gameService.getUserFavoritedGamesPage(
                    currentPage - 1, PAGE_SIZE, sortingMode,
                    actualUser.getCurrentUser());

            setUpGamesPage(gamesPage, FilterMode.FAVORITED_BY_USER,
                    currentPage, model);

            return "games";
        } catch (GameService.IllegalPageValueException e) {
            throw new Page_BadRequestException(e);
        }
    }

    /**
     * Handles GET requests to endpoint '/in-work' and returns a page for
     * viewing a list of the current {@link User} unpublished {@link Game}s.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'mode' - a value of the {@link FilterMode} enumeration that indicates
     * current game filter mode. Always equals to {@link FilterMode#IN_WORK};<br>
     * 'games' - a list of {@link Game} objects that represents a portion of
     * the current {@link User} unpublished {@link Game}s, selected according to
     * the 'page' and 'sort' request parameters;<br>
     * 'currentPage' - a number of the current page. Always equals to
     * the 'page' request parameter;<br>
     * 'totalPages' - a number of pages into which the {@link Game}s
     * are divided;<br>
     * 'user' - the current {@link User} object, adds via
     * {@link CurrentUserToModelAspect}.
     * <p>
     * The page size is equal to the {@link QuesterApplication#PAGE_SIZE}
     * constant.
     *
     * @param model       the Spring MVC model.
     * @param currentPage a request parameter which represents
     *                    the current page number, cannot be missed.
     * @return a page with 'games' template.
     * @throws Page_BadRequestException if the page value is incorrect.
     */
    @RequestMapping("/in-work")
    @CurrentUserToModel
    public String showCurrentUserGamesInWorkPage(HttpServletRequest request,
                                                 Model model,
                                                 @RequestParam(name = "page")
                                                         int currentPage) {
        try {
            val gamesPage =
                    gameService.getUserNotPublishedGamesPage(
                            currentPage - 1, PAGE_SIZE,
                            actualUser.getCurrentUser());

            setUpGamesPage(gamesPage, FilterMode.IN_WORK, currentPage, model);

            return "games";
        } catch (GameService.IllegalPageValueException e) {
            throw new Page_BadRequestException(e);
        }
    }

    private void setUpGamesPage(Page<Game> gamesPage, FilterMode filterMode,
                                Integer currentPage, Model model) {
        model.addAttribute("mode", filterMode);
        model.addAttribute("currentPage", currentPage);

        if (gamesPage.hasContent()) {
            model.addAttribute("games", gamesPage.getContent());
            model.addAttribute("totalPages", gamesPage.getTotalPages());
        } else {
            model.addAttribute("totalPages", 1);
        }
    }

    /**
     * An enumeration used to filter the {@link Game}s by certain parameters.
     */
    public enum FilterMode {
        /**
         * Show all {@link Game}s.
         */
        ALL,
        /**
         * Show only {@link Game}s favorited by the current {@link User}.
         */
        FAVORITED_BY_USER,
        /**
         * Show only {@link Game}s created by certain {@link User}.
         */
        CREATED_BY_USER,
        /**
         * Show only unpublished {@link Game}s from the current {@link User}.
         */
        IN_WORK
    }
}
