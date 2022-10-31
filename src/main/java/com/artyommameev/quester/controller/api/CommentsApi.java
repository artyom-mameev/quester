package com.artyommameev.quester.controller.api;

import com.artyommameev.quester.controller.api.exception.Api_BadRequestException;
import com.artyommameev.quester.controller.api.exception.Api_ForbiddenException;
import com.artyommameev.quester.controller.api.exception.Api_NotFoundException;
import com.artyommameev.quester.dto.CommentDto;
import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.GameService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * A controller for handling Comments API endpoints, which allows to create,
 * edit and delete {@link Comment} objects for a certain {@link Game} via
 * AJAX requests.
 *
 * @author Artyom Mameev
 */
@RestController
@RequestMapping(value = "/api/games/{gameId}/comments")
public class CommentsApi {

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
    public CommentsApi(GameService gameService, ActualUser actualUser) {
        this.gameService = gameService;
        this.actualUser = actualUser;
    }

    /**
     * Handles POST requests to endpoint '/api/games/{gameId}/comments'
     * of the Comments API and allows to create new {@link Comment} for
     * a certain {@link Game}.
     *
     * @param gameId     the path variable that represents an id of the
     *                   {@link Game} to which {@link Comment} should be added
     * @param commentDto the data transfer object with validation
     *                   mechanism.
     * @return a json with 'true' boolean.
     * @throws Api_BadRequestException if a syntax error is detected
     *                                 in the data transfer object.
     * @throws Api_ForbiddenException  if the {@link Game} is not allows to
     *                                 create a {@link Comment} from
     *                                 the current {@link User}.
     * @throws Api_NotFoundException   if the {@link Game} is not found.
     * @see CommentDto
     */
    @PostMapping(produces = "application/json")
    public boolean createComment(@PathVariable long gameId,
                                 @Valid @RequestBody CommentDto commentDto) {
        try {
            gameService.createComment(gameId, actualUser.getCurrentUser(),
                    commentDto.getText());

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
     * Handles PUT requests to endpoint
     * '/api/games/{gameId}/comments/{commentId}' of the Comments API and allows
     * to edit {@link Comment} for a certain {@link Game}.
     *
     * @param gameId     a path variable that represents an id of the
     *                   {@link Game} which {@link Comment} should be edited.
     * @param commentId  a path variable that represents an id of
     *                   the {@link Comment} that should be edited.
     * @param commentDto a data transfer object with validation
     *                   mechanism.
     * @return a json with 'true' boolean.
     * @throws Api_BadRequestException if a syntax error is detected
     *                                 in the data transfer object.
     * @throws Api_ForbiddenException  if the {@link Game} is not allows to edit
     *                                 a {@link Comment} from the current
     *                                 {@link User}.
     * @throws Api_NotFoundException   if the {@link Game} or the {@link Comment}
     *                                 with the given ids are not found.
     * @see CommentDto
     */
    @PutMapping(path = "{commentId}", produces = "application/json")
    public boolean editComment(@PathVariable long gameId,
                               @PathVariable long commentId,
                               @Valid @RequestBody CommentDto commentDto) {
        try {
            gameService.editComment(gameId, commentId,
                    actualUser.getCurrentUser(), commentDto.getText());

            return true;
        } catch (GameService.CommentNotFoundException |
                GameService.GameNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        } catch (GameService.VerificationException e) {
            throw new Api_BadRequestException(e);
        }
    }

    /**
     * Handles DELETE requests to endpoint
     * '/api/games/{gameId}/comments/{commentId}' of the Comments API and allows
     * to remove {@link Comment} for a certain {@link Game}.
     *
     * @param gameId    a path variable that represents an id of the
     *                  {@link Game} which {@link Comment} should be removed.
     * @param commentId a path variable that represents an id of the
     *                  {@link Comment} that should be removed.
     * @return a json with 'true' boolean.
     * @throws Api_ForbiddenException if the {@link Game} is not allows to
     *                                remove a {@link Comment} from
     *                                the current {@link User}.
     * @throws Api_NotFoundException  if the {@link Game} or the {@link Comment}
     *                                with the given ids are not found.
     */
    @DeleteMapping(path = "{commentId}", produces = "application/json")
    public boolean deleteComment(@PathVariable long gameId,
                                 @PathVariable long commentId) {
        try {
            gameService.deleteComment(gameId, commentId,
                    actualUser.getCurrentUser());

            return true;
        } catch (GameService.CommentNotFoundException |
                GameService.GameNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        }
    }
}
