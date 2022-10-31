package com.artyommameev.quester.controller.api;

import com.artyommameev.quester.controller.api.exception.Api_BadRequestException;
import com.artyommameev.quester.controller.api.exception.Api_ForbiddenException;
import com.artyommameev.quester.controller.api.exception.Api_NotFoundException;
import com.artyommameev.quester.dto.gamenode.GameNodeCreationDto;
import com.artyommameev.quester.dto.gamenode.GameNodeEditingDto;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.GameService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * A controller for handling Game Nodes API endpoints, which allows
 * to create, edit or delete {@link GameNode} objects for a certain
 * {@link Game} via AJAX requests.
 *
 * @author Artyom Mameev
 */
@RestController
@RequestMapping(value = "/api/games/{gameId}/nodes")
public class GameNodesApi {

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
    public GameNodesApi(GameService gameService, ActualUser actualUser) {
        this.gameService = gameService;
        this.actualUser = actualUser;
    }

    /**
     * Handles POST requests to endpoint '/api/games/{gameId}/nodes'
     * of the Game Nodes API and allows to create a new {@link GameNode} for
     * a certain {@link Game}.
     *
     * @param gameId              a path variable that represents an id of
     *                            the {@link Game} to which the new
     *                            {@link GameNode} should be created.
     * @param gameNodeCreationDto the data transfer object with
     *                            validation mechanism.
     * @return a json with 'true' boolean.
     * @throws Api_BadRequestException if a syntax error is detected
     *                                 in the data transfer object
     * @throws Api_ForbiddenException  if the {@link Game} is not allows to
     *                                 create a {@link GameNode} from
     *                                 the current {@link User}.
     * @throws Api_NotFoundException   if the {@link Game} with the given id
     *                                 is not found
     * @see GameNodeCreationDto
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public boolean createGameNode(@PathVariable long gameId,
                                  @Valid @RequestBody GameNodeCreationDto
                                          gameNodeCreationDto) {
        try {
            gameService.createGameNode(gameId, actualUser.getCurrentUser(),
                    gameNodeCreationDto.getParentId(),
                    gameNodeCreationDto.getId(), gameNodeCreationDto.getName(),
                    gameNodeCreationDto.getDescription(),
                    gameNodeCreationDto.getType(),
                    (gameNodeCreationDto.getCondition() != null ?
                            gameNodeCreationDto.getCondition().getFlagId() :
                            null),
                    (gameNodeCreationDto.getCondition() != null ?
                            gameNodeCreationDto.getCondition().getFlagState() :
                            null));

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
     * Handles PUT requests to endpoint '/api/games/{gameId}/nodes/{nodeId}'
     * of the Game Nodes API and allows to edit a {@link GameNode} for
     * a certain {@link Game}.
     *
     * @param gameId             a path variable that represents an id of
     *                           the {@link Game} which {@link GameNode}
     *                           should be edited.
     * @param nodeId             a path variable that represents an id of
     *                           the {@link GameNode} that should be edited.
     * @param gameNodeEditingDto the data transfer object with validation
     *                           mechanism.
     * @return a json with 'true' boolean.
     * @throws Api_BadRequestException if a syntax error is detected
     *                                 in the data transfer object
     * @throws Api_ForbiddenException  if the {@link Game} is not allows to edit
     *                                 a {@link GameNode} from the current
     *                                 {@link User}.
     * @throws Api_NotFoundException   if the {@link Game} or the
     *                                 {@link GameNode} with the given ids
     *                                 are not found.
     * @see GameNodeEditingDto
     */
    @PutMapping(path = "{nodeId}", consumes = "application/json",
            produces = "application/json")
    public boolean editGameNode(@PathVariable long gameId,
                                @PathVariable String nodeId,
                                @Valid @RequestBody GameNodeEditingDto
                                        gameNodeEditingDto) {
        try {
            gameService.editGameNode(gameId, nodeId, actualUser.getCurrentUser(),
                    gameNodeEditingDto.getName(),
                    gameNodeEditingDto.getDescription(),
                    (gameNodeEditingDto.getCondition() != null ?
                            gameNodeEditingDto.getCondition().getFlagId() :
                            null),
                    (gameNodeEditingDto.getCondition() != null ?
                            gameNodeEditingDto.getCondition().getFlagState() :
                            null));

            return true;
        } catch (GameService.GameNotFoundException |
                GameService.NodeNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        } catch (GameService.VerificationException e) {
            throw new Api_BadRequestException(e);
        }
    }

    /**
     * Handles DELETE requests to endpoint '/api/games/{gameId}/nodes/{nodeId}'
     * of the Game Nodes API and allows to remove a {@link GameNode} for
     * a certain {@link Game}.
     *
     * @param gameId a path variable that represents an id of the
     *               {@link Game} which {@link GameNode} should be removed.
     * @param nodeId a path variable that represents an id of the
     *               {@link GameNode} that should be removed.
     * @return a json with 'true' boolean
     * @throws Api_BadRequestException if the request to delete does not
     *                                 make sense.
     * @throws Api_ForbiddenException  if the {@link Game} is not allows to
     *                                 remove a {@link GameNode} from
     *                                 the current {@link User}.
     * @throws Api_NotFoundException   if the {@link Game} or the
     *                                 {@link GameNode} with the given ids
     *                                 are not found.
     */
    @DeleteMapping(path = "{nodeId}", produces = "application/json")
    public boolean deleteGameNode(@PathVariable long gameId,
                                  @PathVariable String nodeId) {
        try {
            gameService.deleteGameNode(gameId, nodeId,
                    actualUser.getCurrentUser());

            return true;
        } catch (GameService.GameNotFoundException |
                GameService.NodeNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        } catch (GameService.VerificationException e) {
            throw new Api_BadRequestException(e);
        }
    }

}
