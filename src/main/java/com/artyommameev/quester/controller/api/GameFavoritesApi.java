package com.artyommameev.quester.controller.api;

import com.artyommameev.quester.controller.api.exception.Api_ForbiddenException;
import com.artyommameev.quester.controller.api.exception.Api_NotFoundException;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.GameService;
import org.springframework.web.bind.annotation.*;

/**
 * A controller for handling Game Favorites API endpoints, which allows
 * to add or remove a {@link Game} from the current {@link User} favorites via
 * AJAX requests.
 *
 * @author Artyom Mameev
 */
@RestController
@RequestMapping(value = "/api/games/{gameId}/favorites")
public class GameFavoritesApi {

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
    public GameFavoritesApi(ActualUser actualUser, GameService gameService) {
        this.actualUser = actualUser;
        this.gameService = gameService;
    }

    /**
     * Handles POST requests to endpoint '/api/games/{gameId}/favorites'
     * of the Game Favorites API and allows to add a {@link Game} to the current
     * {@link User} favorites.
     *
     * @param gameId a path variable that represents an id of the
     *               {@link Game} that should be added to the current
     *               {@link User}'s favorites
     * @return a json with 'true' boolean.
     * @throws Api_ForbiddenException if the {@link Game} is not allows to be
     *                                favorited from the current {@link User}.
     * @throws Api_NotFoundException  if the {@link Game} with the given id
     *                                is not found.
     */
    @PostMapping(produces = "application/json")
    public boolean createFavorite(@PathVariable long gameId) {
        try {
            gameService.makeFavorited(gameId, actualUser.getCurrentUser());

            return true;
        } catch (GameService.GameNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        }
    }

    /**
     * Handles DELETE requests to endpoint '/api/games/{gameId}/favorites'
     * of the Game Favorites API and allows to remove a {@link Game} from the
     * current {@link User}'s favorites.
     *
     * @param gameId a path variable that represents an id of the
     *               {@link Game} that should be removed from the current
     *               {@link User}'s favorites.
     * @return a json with 'true' boolean.
     * @throws Api_ForbiddenException if the {@link Game} is not allows to be
     *                                unfavorited from the current {@link User}.
     * @throws Api_NotFoundException  if the {@link Game} with the given id
     *                                is not found.
     */
    @DeleteMapping(produces = "application/json")
    public boolean removeFavorite(@PathVariable long gameId) {
        try {
            gameService.makeUnfavorited(gameId, actualUser.getCurrentUser());

            return true;
        } catch (GameService.GameNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        }
    }
}
