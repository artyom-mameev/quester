package com.artyommameev.quester.controller.api;

import com.artyommameev.quester.controller.api.exception.Api_BadRequestException;
import com.artyommameev.quester.controller.api.exception.Api_ForbiddenException;
import com.artyommameev.quester.controller.api.exception.Api_NotFoundException;
import com.artyommameev.quester.dto.GameDto;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.Game.JsonReadyGame;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.GameService;
import lombok.val;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * A controller for handling Games API endpoints, which allows to create,
 * edit and remove {@link Game} objects via AJAX requests.
 *
 * @author Artyom Mameev
 */
@RestController
@RequestMapping(value = "/api/games")
public class GamesApi {

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
    public GamesApi(GameService gameService, ActualUser actualUser) {
        this.gameService = gameService;
        this.actualUser = actualUser;
    }

    /**
     * Handles POST requests to endpoint '/api/games' of the Games API
     * and allows to create a new {@link Game}.
     *
     * @param gameDto the data transfer object with validation mechanism
     * @return a json with 'true' boolean.
     * @throws Api_BadRequestException if a syntax error is detected
     *                                 in the data transfer object.
     * @see GameDto
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public long createGame(@Valid @RequestBody GameDto gameDto) {
        try {
            val savedGame = gameService.createGame(gameDto.getName(),
                    gameDto.getDescription(), gameDto.getLanguage(),
                    actualUser.getCurrentUser(), false);

            return savedGame.getId();
        } catch (GameService.VerificationException e) {
            throw new Api_BadRequestException(e);
        }
    }

    /**
     * Handles PUT requests to endpoint '/api/games/{gameId}' of the Games API
     * and allows to edit a certain {@link Game} information.
     *
     * @param gameId  a path variable that represents an id of the
     *                {@link Game} that should be edited.
     * @param gameDto the data transfer object with validation
     *                mechanism.
     * @return a json with 'true' boolean.
     * @throws Api_BadRequestException if a syntax error is detected
     *                                 in the data transfer object.
     * @throws Api_ForbiddenException  if the {@link Game} is not allows
     *                                 to be edited from the current
     *                                 {@link User}.
     * @throws Api_NotFoundException   if the {@link Game} with the given
     *                                 id is not found.
     * @see GameDto
     */
    @PutMapping(path = "{gameId}", consumes = "application/json",
            produces = "application/json")
    public boolean editGame(@PathVariable long gameId,
                            @Valid @RequestBody GameDto gameDto) {
        try {
            gameService.editGame(gameId, gameDto.getName(),
                    gameDto.getDescription(), gameDto.getLanguage(),
                    actualUser.getCurrentUser(), gameDto.isPublished());

            return true;
        } catch (GameService.GameNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        } catch (GameService.VerificationException e) {
            throw new Api_BadRequestException(e);
        }
    }

    /**
     * Handles DELETE requests to endpoint '/api/games/{gameId}'
     * of the Games API and allows to remove a certain {@link Game}.
     *
     * @param gameId a path variable that represents an id of the
     *               {@link Game} that should be removed.
     * @return a json with 'true' boolean.
     * @throws Api_ForbiddenException if the {@link Game} is not allows
     *                                to be removed from the current {@link User}
     * @throws Api_NotFoundException  if the {@link Game} with the given id
     *                                is not found.
     * @see GameDto
     */
    @DeleteMapping(path = "{gameId}", produces = "application/json")
    public boolean deleteGame(@PathVariable long gameId) {
        try {
            gameService.deleteGame(gameId, actualUser.getCurrentUser());

            return true;
        } catch (GameService.GameNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        }
    }

    /**
     * Handles GET requests to endpoint '/api/games/{gameId}'
     * of the Games API and allows to get a certain {@link Game} in json format.
     *
     * @param gameId a path variable that represents an id of the
     *               {@link Game} that should be received as json.
     * @return the {@link JsonReadyGame}.
     * @throws Api_ForbiddenException if the {@link Game} is not allows
     *                                to be viewed from the current {@link User}.
     * @throws Api_NotFoundException  if the {@link Game} with the given id
     *                                is not found.
     */
    @GetMapping(path = "{gameId}", produces = "application/json")
    public JsonReadyGame getGame(@PathVariable long gameId) {
        try {
            return gameService.getGameJson(gameId);
        } catch (GameService.GameNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        }
    }
}