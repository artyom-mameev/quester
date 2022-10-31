package com.artyommameev.quester.controller.api;

import com.artyommameev.quester.controller.api.exception.Api_ForbiddenException;
import com.artyommameev.quester.controller.api.exception.Api_NotFoundException;
import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.Review;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * A controller for handling Users API endpoints, which allows admin
 * {@link User} to ban and unban normal {@link User} and remove all
 * {@link Game}s, {@link Comment}s and {@link Review}s created by that
 * {@link User} via AJAX requests.
 *
 * @author Artyom Mameev
 */
@RestController
@RequestMapping(value = "/api/users/{userId}")
public class UsersApi {

    private final UserService userService;
    private final ActualUser actualUser;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param userService a service that allows to query, interact and save
     *                    {@link User} objects.
     * @param actualUser  the {@link ActualUser} abstraction which represents
     *                    current normal or oAuth2 user.
     * @see UserService
     * @see ActualUser
     */
    public UsersApi(UserService userService, ActualUser actualUser) {
        this.userService = userService;
        this.actualUser = actualUser;
    }

    /**
     * Handles POST requests to endpoint '/api/users/{userId}/ban'
     * of the Users API and allows admin {@link User} to ban normal
     * {@link User}.
     *
     * @param userId a path variable that represents an id of the
     *               {@link User} that should be be banned
     * @return a json with 'true' boolean.
     * @throws Api_ForbiddenException if the {@link User} is not allowed to be
     *                                banned from the current {@link User}
     * @throws Api_NotFoundException  if the {@link User} is not found
     */
    @PostMapping(path = "ban", produces = "application/json")
    public boolean banUser(@PathVariable long userId) {
        try {
            userService.setEnabled(userId, false,
                    actualUser.getCurrentUser());

            return true;
        } catch (UserService.UserNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (UserService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        }
    }

    /**
     * Handles DELETE requests to endpoint '/api/users/{userId}/ban'
     * of the Users API and allows admin {@link User} to unban normal
     * {@link User}.
     *
     * @param userId a path variable that represents an id of the
     *               {@link User} that should be unbanned.
     * @return a json with 'true' boolean.
     * @throws Api_ForbiddenException if the {@link User} is not allowed to be
     *                                unbanned from the current {@link User}.
     * @throws Api_NotFoundException  if the {@link User} with the given id
     *                                is not found.
     */
    @DeleteMapping(path = "ban", produces = "application/json")
    public boolean unbanUser(@PathVariable long userId) {
        try {
            userService.setEnabled(userId, true,
                    actualUser.getCurrentUser());

            return true;
        } catch (UserService.UserNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (UserService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        }
    }

    /**
     * Handles DELETE requests to endpoint '/api/users/{userId}/games'
     * of the Users API and allows admin {@link User} to remove all
     * {@link Game}s created by certain {@link User}.
     *
     * @param userId a path variable that represents an id of the
     *               {@link User} whose {@link Game}s should be removed.
     * @return a json with 'true' boolean.
     * @throws Api_ForbiddenException if the {@link Game}s from the {@link User}
     *                                is not allowed to be removed
     *                                from the current {@link User}.
     * @throws Api_NotFoundException  if the {@link Game}s from the {@link User}
     *                                with the given id is not found.
     */
    @DeleteMapping(path = "games", produces = "application/json")
    public boolean deleteCreatedGames(@PathVariable long userId) {
        try {
            userService.deleteUserGames(userId, actualUser.getCurrentUser());

            return true;
        } catch (UserService.UserNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (UserService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        }
    }

    /**
     * Handles DELETE requests to endpoint '/api/users/{userId}/reviews'
     * of the Users API and allows admin {@link User} to remove all
     * {@link Review}s created by certain normal {@link User}.
     *
     * @param userId a path variable that represents an id of the
     *               {@link User} whose {@link Review}s should be removed.
     * @return a json with 'true' boolean.
     * @throws Api_ForbiddenException if the {@link Review}s from the
     *                                {@link User} is not allowed to be
     *                                removed from the current {@link User}.
     * @throws Api_NotFoundException  if the {@link Review}s from the
     *                                {@link User} with the given id
     *                                is not found.
     */
    @DeleteMapping(path = "reviews", produces = "application/json")
    public boolean deleteCreatedReviews(@PathVariable long userId) {
        try {
            userService.deleteUserReviews(userId, actualUser.getCurrentUser());

            return true;
        } catch (UserService.UserNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (UserService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        }
    }

    /**
     * Handles DELETE requests to endpoint '/api/users/{userId}/comments'
     * of the Users API and allows admin {@link User} to remove {@link Comment}s
     * created by certain normal {@link User}.
     *
     * @param userId a path variable that represents an id of the
     *               {@link User} whose {@link Comment}s should be removed.
     * @return a json with 'true' boolean.
     * @throws Api_ForbiddenException if the {@link Comment}s from the
     *                                {@link User} is not allowed to be
     *                                removed from the current {@link User}.
     * @throws Api_NotFoundException  if the {@link Comment}s from
     *                                the {@link User} is not found.
     */
    @DeleteMapping(path = "comments", produces = "application/json")
    public boolean deleteCreatedComments(@PathVariable long userId) {
        try {
            userService.deleteUserComments(userId, actualUser.getCurrentUser());

            return true;
        } catch (UserService.UserNotFoundException e) {
            throw new Api_NotFoundException(e);
        } catch (UserService.ForbiddenException e) {
            throw new Api_ForbiddenException(e);
        }
    }
}