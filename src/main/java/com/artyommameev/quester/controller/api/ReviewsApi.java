package com.artyommameev.quester.controller.api;

import com.artyommameev.quester.controller.api.exception.Api_BadRequestException;
import com.artyommameev.quester.controller.api.exception.Api_ForbiddenException;
import com.artyommameev.quester.controller.api.exception.Api_NotFoundException;
import com.artyommameev.quester.dto.ReviewDto;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.Review;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.GameService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * A controller for handling Reviews API endpoints, which allows to create
 * and edit current {@link User}'s {@link Review} for a certain {@link Game}
 * via AJAX requests.
 *
 * @author Artyom Mameev
 */
@RestController
@RequestMapping(value = "/api/games/{gameId}/rate")
public class ReviewsApi {

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
    public ReviewsApi(GameService gameService, ActualUser actualUser) {
        this.gameService = gameService;
        this.actualUser = actualUser;
    }

    /**
     * Handles POST requests to endpoint '/api/games/{gameId}/rate'
     * of the Reviews API and allows the current {@link User} to create a new
     * {@link Review} for a certain {@link Game}.
     *
     * @param gameId    a path variable that represents an id of the
     *                  {@link Game} to which {@link Review} should be created.
     * @param reviewDto the data transfer object with validation mechanism.
     * @return a json with 'true' boolean.
     * @throws Api_BadRequestException if a syntax error is detected
     *                                 in the data transfer object.
     * @throws Api_ForbiddenException  if the {@link Game} is not allows to
     *                                 create a {@link Review} from the
     *                                 current {@link User}.
     * @throws Api_NotFoundException   if the {@link Game} with the given
     *                                 id is not found.
     * @see ReviewDto
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public boolean createReview(@RequestBody @Valid ReviewDto reviewDto,
                                @PathVariable long gameId) {
        try {
            gameService.createReview(gameId, reviewDto.getRating(),
                    actualUser.getCurrentUser());

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
     * Handles PUT requests to endpoint '/api/games/{gameId}/rate'
     * of the Reviews API and allows the current {@link User} to edit
     * a {@link Review} for a certain {@link Game}.
     *
     * @param gameId    a path variable that represents an id of the
     *                  {@link Game} which {@link Review} should be edited.
     * @param reviewDto the data transfer object with validation mechanism.
     * @return a json with 'true' boolean.
     * @throws Api_BadRequestException if a syntax error is detected
     *                                 in the data transfer object.
     * @throws Api_ForbiddenException  if the {@link Game} is not allows to edit
     *                                 a {@link Review} from the current
     *                                 {@link User}.
     * @throws Api_NotFoundException   if the {@link Game} or the {@link Review}
     *                                 with the given ids are not found.
     * @see ReviewDto
     */
    @PutMapping(consumes = "application/json", produces = "application/json")
    public boolean editReview(@RequestBody @Valid ReviewDto reviewDto,
                              @PathVariable long gameId) {
        try {
            gameService.editReview(gameId, reviewDto.getRating(),
                    actualUser.getCurrentUser());

            return true;
        } catch (GameService.GameNotFoundException |
                GameService.ReviewNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (GameService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        } catch (GameService.VerificationException e) {
            throw new Api_BadRequestException(e);
        }
    }
}
