package com.artyommameev.quester.service;

import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.Game.JsonReadyGame;
import com.artyommameev.quester.entity.Review;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.entity.gamenode.ConditionNode;
import com.artyommameev.quester.entity.gamenode.FlagNode;
import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.repository.GameRepository;
import com.sun.istack.Nullable;
import lombok.NonNull;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service that allows to query, interact, and save {@link Game} objects.
 * <p>
 * All exceptions related to verification of the {@link Game} objects
 * constraints are handled as a {@link VerificationException} in order to keep
 * abstraction levels apart and with an eye toward the API design where's no
 * need to handle verification exceptions differently - the service is designed
 * to handle client data whose errors do not make sense to handle differently -
 * it is enough to return an error to the client, so the service wraps
 * verification errors in an exceptions corresponding to the current abstraction
 * level, and the true cause of the exception (in order to notify the client
 * about it) can be retrieved from the root exception.
 *
 * @author Artyom Mameev
 */
@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param gameRepository a repository for querying and saving {@link Game}
     *                       objects.
     * @see GameRepository
     */
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Returns a {@link Game} by id from the database.
     *
     * @param gameId the id of the {@link Game} that should be returned.
     * @return a {@link Game} with the given id.
     * @throws GameNotFoundException if a {@link Game} with the given id is
     *                               not found in the database.
     */
    public Game getGame(long gameId) throws GameNotFoundException {
        val optionalGame = gameRepository.findById(gameId);

        return optionalGame.orElseThrow(() ->
                new GameNotFoundException("The game with id " + gameId +
                        " is not found"));
    }

    /**
     * Saves a {@link Game} to the database.
     *
     * @param game the {@link Game} that should be saved.
     * @return a saved {@link Game} instance with an assigned database id.
     */
    public Game saveGame(@NonNull Game game) {
        return gameRepository.save(game);
    }

    /**
     * Returns a {@link Page} with published {@link Game}s.
     *
     * @param sortingMode a sorting mode of the {@link Game}s.
     * @param page        the {@link Page} number.
     * @param size        the {@link Page} size.
     * @return a {@link Page} with published {@link Game}s according to
     * the given {@link Page} size and {@link Page} number.
     * @throws IllegalPageValueException if the {@link Page} number is less
     *                                   than 0.
     * @see SortingMode
     */
    public Page<Game> getPublishedGamesPage(int page, int size,
                                            @NonNull SortingMode sortingMode)
            throws IllegalPageValueException {
        if (page < 0) {
            throw new IllegalPageValueException("Page value cannot be" +
                    " less than 0!");
        }

        switch (sortingMode) {
            case OLDEST:
                return gameRepository
                        .findGamesByPublishedIsTrueOrderByDate(
                                PageRequest.of(page, size));
            case NEWEST:
                return gameRepository
                        .findGamesByPublishedIsTrueOrderByDateDesc(
                                PageRequest.of(page, size));
            case RATING:
                return gameRepository
                        .findGamesByPublishedIsTrueOrderByRatingDesc(
                                PageRequest.of(page, size));
            default:
                throw new RuntimeException("Unknown enum value: " +
                        sortingMode);
        }
    }

    /**
     * Creates a new {@link GameNode} for a specific {@link Game} and saves it
     * into the database.
     * <p>
     * Depending on the {@link GameNode} type, certain method parameters may not
     * be used and therefore may be omitted. See the documentation of a specific
     * {@link GameNode} implementations to see constraints of adding particular
     * node types.
     *
     * @param gameId             an id of the {@link Game} to which
     *                           the new {@link GameNode} should be added.
     * @param user               a {@link User} that tries to create
     *                           the new {@link GameNode}.
     * @param parentId           an id of the new {@link GameNode}'s parent.
     * @param id                 ab id of the new {@link GameNode}.
     * @param name               a name of the new {@link GameNode}.
     * @param description        a description of the new {@link GameNode}.
     * @param type               a type of the new {@link GameNode}.
     * @param conditionFlagId    an id of the {@link FlagNode}, which should
     *                           trigger the new {@link GameNode}
     *                           at a specific {@link FlagNode} state (only if
     *                           the new {@link GameNode} is a
     *                           {@link ConditionNode}).
     * @param conditionFlagState a {@link FlagNode} state at which the
     *                           new node should be triggered (only if
     *                           the new {@link GameNode} is a
     *                           {@link ConditionNode}).
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws ForbiddenException    if the {@link User} is not allowed to
     *                               create a {@link GameNode} for
     *                               the {@link Game}.
     * @throws VerificationException if a verification error occurs when
     *                               creating the new {@link GameNode}.
     */
    public void createGameNode(long gameId, @NonNull User user,
                               String parentId, String id, String name,
                               String description, GameNode.NodeType type,
                               String conditionFlagId,
                               GameNode.Condition.FlagState conditionFlagState)
            throws GameNotFoundException, ForbiddenException,
            VerificationException {
        val game = getGame(gameId);

        try {
            game.addNode(id, parentId, name, description, type,
                    conditionFlagId, conditionFlagState, user);

            saveGame(game);
        } catch (Game.ForbiddenManipulationException e) {
            throw new ForbiddenException(e);
        } catch (Game.NodeVerificationException |
                Game.IllegalRootNodeTypeException | Game.NotRootNodeException e) {
            throw new VerificationException(e);
        }
    }

    /**
     * Edits an existing {@link GameNode} of a specific {@link Game} and saves
     * it into the database.
     * <p>
     * Depending on the {@link GameNode} type, certain method parameters may not
     * be used and therefore may be omitted. See the documentation of a specific
     * {@link GameNode} implementations to see constraints of adding particular
     * node types.
     *
     * @param gameId             an id of the {@link Game} which
     *                           {@link GameNode} should be edited.
     * @param nodeId             an id of the {@link GameNode} that should be
     *                           edited.
     * @param user               a {@link User} that tries to edit
     *                           the {@link GameNode}.
     * @param name               a new name of the {@link GameNode}.
     * @param description        a new description of the {@link GameNode}.
     * @param conditionFlagId    a new id of the {@link FlagNode}, which
     *                           should trigger the new {@link GameNode}
     *                           at a specific {@link FlagNode} state (only if
     *                           the new {@link GameNode} is a
     *                           {@link ConditionNode}).
     * @param conditionFlagState a new {@link FlagNode} state at which
     *                           the new {@link GameNode} should be triggered
     *                           (only if the new {@link GameNode} is a
     *                           {@link ConditionNode}).
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws NodeNotFoundException if the {@link GameNode} is not found.
     * @throws ForbiddenException    if the {@link User} is not allowed to edit
     *                               a {@link GameNode} for the {@link Game}.
     * @throws VerificationException if a verification error occurs when
     *                               editing the {@link GameNode}.
     */
    public void editGameNode(long gameId, @NonNull String nodeId,
                             @NonNull User user, String name,
                             String description, String conditionFlagId,
                             GameNode.Condition.FlagState conditionFlagState)
            throws GameNotFoundException, ForbiddenException,
            NodeNotFoundException, VerificationException {
        val game = getGame(gameId);

        try {
            game.editNode(nodeId, name, description, conditionFlagId,
                    conditionFlagState, user);

            saveGame(game);
        } catch (Game.NodeVerificationException |
                Game.RootNodeNotExistsException e) {
            throw new VerificationException(e);
        } catch (Game.ForbiddenManipulationException e) {
            throw new ForbiddenException(e);
        } catch (Game.NodeNotFoundException e) {
            throw new NodeNotFoundException(e);
        }
    }

    /**
     * Removes an existing {@link GameNode} of a specific {@link Game} from
     * the database.
     *
     * @param gameId an id of the {@link Game} which {@link GameNode} should
     *               be removed.
     * @param nodeId an id of the {@link GameNode} that should be removed.
     * @param user   a {@link User} that tries to remove the {@link GameNode}.
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws NodeNotFoundException if the {@link GameNode} is not found.
     * @throws ForbiddenException    if the {@link User} is not allowed
     *                               to remove the {@link GameNode}.
     * @throws VerificationException if the {@link GameNode} cannot be removed.
     */
    public void deleteGameNode(long gameId, String nodeId,
                               @NonNull User user)
            throws GameNotFoundException, NodeNotFoundException,
            ForbiddenException, VerificationException {
        val game = getGame(gameId);

        try {
            game.deleteNode(nodeId, user);

            saveGame(game);
        } catch (Game.NodeNotFoundException e) {
            throw new NodeNotFoundException(e);
        } catch (Game.ForbiddenManipulationException e) {
            throw new ForbiddenException(e);
        } catch (Game.RootNodeNotExistsException |
                Game.NodeVerificationException e) {
            throw new VerificationException(e);
        }
    }

    /**
     * Creates a new {@link Comment} to a {@link Game}.
     *
     * @param gameId an id of the {@link Game} to which the new {@link Comment}
     *               should be created.
     * @param user   a {@link User} that tries to create the new
     *               {@link Comment}.
     * @param text   the {@link Comment} text.
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws ForbiddenException    if the {@link User} is not allowed to
     *                               create a new {@link Comment} for
     *                               the {@link Game}.
     * @throws VerificationException if a verification error occurs when
     *                               creating the new {@link Comment}.
     */
    public void createComment(long gameId, @NonNull User user, String text)
            throws GameNotFoundException, ForbiddenException,
            VerificationException {
        val game = getGame(gameId);

        try {
            game.addComment(text, user);

            saveGame(game);
        } catch (Game.NotPublishedException e) {
            throw new ForbiddenException(e);
        } catch (Game.EmptyStringException | Game.NullValueException e) {
            throw new VerificationException(e);
        }
    }

    /**
     * Edits an existing {@link Comment} for a {@link Game}.
     *
     * @param gameId    an id of the {@link Game} which {@link Comment}
     *                  should be edited.
     * @param commentId an id of the {@link Comment} that should be edited.
     * @param user      a {@link User} that tries to edit the {@link Comment}.
     * @param text      the new {@link Comment} text.
     * @throws GameNotFoundException    if the {@link Game} is not found.
     * @throws CommentNotFoundException if the {@link Comment} is not found.
     * @throws ForbiddenException       if the {@link User} is not allowed
     *                                  to edit the {@link Comment} for
     *                                  the {@link Game}.
     * @throws VerificationException    if a verification error occurs when
     *                                  editing the {@link Comment}.
     */
    public void editComment(long gameId, long commentId, @NonNull User user,
                            String text) throws GameNotFoundException,
            ForbiddenException, CommentNotFoundException, VerificationException {
        val game = getGame(gameId);

        try {
            game.editComment(commentId, text, user);

            saveGame(game);
        } catch (Game.NotPublishedException |
                Game.ForbiddenManipulationException e) {
            throw new ForbiddenException(e);
        } catch (Game.CommentNotFoundException e) {
            throw new CommentNotFoundException(e);
        } catch (Game.EmptyStringException | Game.NullValueException e) {
            throw new VerificationException(e);
        }
    }

    /**
     * Removes an existing {@link Comment} of a {@link Game} from the
     * database.
     *
     * @param gameId    an id of the {@link Game} which {@link Comment}
     *                  should be removed.
     * @param commentId an id of the {@link Comment} that should be removed.
     * @param user      a {@link User} that tries to remove the {@link Comment}.
     * @throws GameNotFoundException    if the {@link Game} is not found.
     * @throws CommentNotFoundException if the {@link Comment} is not found.
     * @throws ForbiddenException       if the {@link User} is not allowed to
     *                                  remove a {@link Comment} for
     *                                  the {@link Game}.
     */
    public void deleteComment(long gameId, long commentId, @NonNull User user)
            throws GameNotFoundException, ForbiddenException,
            CommentNotFoundException {
        val game = getGame(gameId);

        try {
            game.deleteComment(commentId, user);

            saveGame(game);
        } catch (Game.NotPublishedException |
                Game.ForbiddenManipulationException e) {
            throw new ForbiddenException(e);
        } catch (Game.CommentNotFoundException e) {
            throw new CommentNotFoundException(e);
        }
    }

    /**
     * Creates a new {@link Game} and saves it into the database.
     *
     * @param name        a name of the {@link Game}.
     * @param description a description of the {@link Game}.
     * @param language    a language of the {@link Game}.
     * @param user        a {@link User} that tries to create the {@link Game}.
     * @param published   a status indicating whether the {@link Game}
     *                    should be published.
     * @return the saved {@link Game} instance with an assigned database id.
     * @throws VerificationException if a verification error occurs when
     *                               creating the {@link Game}.
     */
    public Game createGame(String name, String description, String language,
                           @NonNull User user, boolean published)
            throws VerificationException {
        try {
            val game = new Game(name, description, language, user, published);

            return saveGame(game);
        } catch (Game.EmptyStringException | Game.NullValueException e) {
            throw new VerificationException(e);
        }
    }

    /**
     * Edits an existing {@link Game} information in the database.
     *
     * @param gameId      an id of the {@link Game} that should be edited.
     * @param name        a new {@link Game} name.
     * @param description a new {@link Game} description.
     * @param language    a new {@link Game} language.
     * @param published   a status indicating whether the {@link Game} should
     *                    be published.
     * @param user        a {@link User} that tries to edit the {@link Game}.
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws ForbiddenException    if the {@link User} is not allowed to edit
     *                               the {@link Game} information.
     * @throws VerificationException if a verification error occurs when
     *                               editing the {@link Game}.
     */
    public void editGame(long gameId, String name, String description,
                         String language, @NonNull User user,
                         boolean published) throws GameNotFoundException,
            ForbiddenException, VerificationException {
        val game = getGame(gameId);

        try {
            game.updateName(name, user);
            game.updateDescription(description, user);
            game.updateLanguage(language, user);
            game.updatePublished(published, user);

            saveGame(game);
        } catch (Game.ForbiddenManipulationException e) {
            throw new ForbiddenException(e);
        } catch (Game.EmptyStringException | Game.NullValueException e) {
            throw new VerificationException(e);
        }
    }

    /**
     * Removes an existing {@link Game} from the database.
     *
     * @param gameId an id of the {@link Game} that should be removed.
     * @param user   a {@link User} that tries to remove the {@link Game}.
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws ForbiddenException    if the {@link User} is not allowed
     *                               to remove the {@link Game}.
     */
    public void deleteGame(long gameId, @NonNull User user)
            throws GameNotFoundException, ForbiddenException {
        val game = getGame(gameId);

        if (!game.canBeModifiedFrom(user)) {
            throw new ForbiddenException("The game with the id " + game +
                    " cannot be modified by user " + user.getUsername());
        }

        deleteGame(game);
    }

    /**
     * Creates a new {@link Review} for a {@link Game}.
     *
     * @param gameId an id of the {@link Game} to which the {@link Review}
     *               should be created.
     * @param user   a {@link User} that tries to create the {@link Review}.
     *               for the {@link Game}
     * @param rating the {@link Review} rating.
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws ForbiddenException    if the {@link User} is not allowed to
     *                               create a {@link Review} for
     *                               the {@link Game}.
     * @throws VerificationException if a verification error occurs when
     *                               creating the {@link Review}.
     */
    public void createReview(long gameId, String rating, @NonNull User user)
            throws GameNotFoundException, ForbiddenException,
            VerificationException {
        val game = getGame(gameId);

        try {
            game.addReview(Review.Rating.fromString(rating), user);

            saveGame(game);
        } catch (Game.NotPublishedException e) {
            throw new ForbiddenException(e);
        } catch (Review.NullRatingException | Review.NotNumberRatingException |
                Review.WrongRatingException e) {
            throw new VerificationException(e);
        }
    }

    /**
     * Edits a current {@link User}'s existing {@link Review} for a specific
     * {@link Game}.
     *
     * @param gameId an id of the {@link Game} which {@link Review}
     *               should be edited.
     * @param user   the {@link User} that tries to edit their {@link Review}.
     * @param rating the new rating of the {@link Review}.
     * @throws GameNotFoundException   if the {@link Game} is not found.
     * @throws ReviewNotFoundException if the {@link Review} is not found.
     * @throws ForbiddenException      if the {@link User} is not allowed to edit
     *                                 their {@link Review} for the {@link Game}.
     * @throws VerificationException   if a verification error occurs when
     *                                 editing the {@link Review}.
     */
    public void editReview(long gameId, String rating, @NonNull User user)
            throws GameNotFoundException, ForbiddenException,
            ReviewNotFoundException, VerificationException {
        val game = getGame(gameId);

        try {
            game.editReview(Review.Rating.fromString(rating), user);

            saveGame(game);
        } catch (Game.NotPublishedException e) {
            throw new ForbiddenException(e);
        } catch (Game.ReviewNotFoundException e) {
            throw new ReviewNotFoundException(e);
        } catch (Review.NullRatingException | Review.NotNumberRatingException |
                Review.WrongRatingException e) {
            throw new VerificationException(e);
        }
    }

    /**
     * Makes a {@link Game} favorited by a certain {@link User}.
     *
     * @param gameId an id of the {@link Game} that should be favorited.
     * @param user   a {@link User} that tries to favorite the {@link Game}.
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws ForbiddenException    if the {@link Game} cannot be favorited
     *                               from the {@link User}.
     */
    public void makeFavorited(long gameId, @NonNull User user)
            throws GameNotFoundException, ForbiddenException {
        val game = getGame(gameId);

        try {
            game.addFavoritedUser(user);

            saveGame(game);
        } catch (Game.NotPublishedException e) {
            throw new ForbiddenException(e);
        }
    }

    /**
     * Makes a {@link Game} unfavorited by a certain {@link User}.
     *
     * @param gameId an id of the {@link Game} that should be unfavorited.
     * @param user   the {@link User} that tries to unfavorite the {@link Game}.
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws ForbiddenException    if the {@link Game} cannot be unfavorited
     *                               from the {@link User}.
     */
    public void makeUnfavorited(long gameId, @NonNull User user)
            throws GameNotFoundException, ForbiddenException {
        val game = getGame(gameId);

        try {
            game.removeFavoritedUser(user);

            saveGame(game);
        } catch (Game.NotPublishedException e) {
            throw new ForbiddenException(e);
        }
    }

    /**
     * Returns a {@link Game} for view purposes.
     *
     * @param gameId an id of the {@link Game} that should be returned.
     * @param user   a {@link User} that tries to view the {@link Game}.
     * @return a {@link Game} with a given id.
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws ForbiddenException    if the {@link User} is not allowed to view
     *                               the {@link Game}.
     */
    public Game getGameForView(long gameId, @Nullable User user)
            throws ForbiddenException, GameNotFoundException {
        val game = getGame(gameId);

        if (!game.canBeViewedFrom(user)) {
            throw new ForbiddenException("The game with the id " + game +
                    " cannot be viewed from user " + user.getUsername());
        }

        return game;
    }

    /**
     * Returns a {@link Game} for edit purposes.
     *
     * @param gameId an id of the {@link Game} that should be returned.
     * @param user   a {@link User} that tries to edit the {@link Game}.
     * @return a {@link Game} with the given id.
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws ForbiddenException    if the {@link User} is not allowed to edit
     *                               the {@link Game}.
     */
    public Game getGameForEdit(long gameId, @NonNull User user)
            throws ForbiddenException, GameNotFoundException {
        val game = getGame(gameId);

        if (!game.canBeModifiedFrom(user)) {
            throw new ForbiddenException("The game with the id " + game +
                    " cannot be modified by user " + user.getUsername());
        }

        return game;
    }

    /**
     * Returns a {@link Game} that can be sent as json.
     *
     * @param gameId an id of the {@link Game} that need to be sent as json.
     * @return a {@link JsonReadyGame}.
     * @throws GameNotFoundException if the {@link Game} is not found.
     * @throws ForbiddenException    if the {@link Game} cannot be sent as json.
     */
    public JsonReadyGame getGameJson(long gameId) throws ForbiddenException,
            GameNotFoundException {
        val game = getGame(gameId);

        try {
            return game.getJsonReadyGame();
        } catch (Game.NotPublishedException e) {
            throw new ForbiddenException(e);
        }
    }

    /**
     * Returns a {@link Page} with {@link Game}s published by a certain
     * {@link User}.
     *
     * @param userId      an id of the {@link User} which published
     *                    {@link Game}s should be returned.
     * @param sortingMode a {@link SortingMode} of the {@link Game}s.
     * @param page        the {@link Page} number.
     * @param size        the {@link Page} size.
     * @return a {@link Page} with the {@link Game}s published by the given
     * {@link User} according to the given {@link Page} number and {@link Page}
     * size.
     * @throws IllegalPageValueException if the {@link Page} number is less
     *                                   than 0.
     * @see SortingMode
     */
    public Page<Game> getUserPublishedGamesPage(int page, int size,
                                                @NonNull SortingMode sortingMode,
                                                long userId)
            throws IllegalPageValueException {
        if (page < 0) {
            throw new IllegalPageValueException("Page value cannot be" +
                    " less than 0!");
        }

        switch (sortingMode) {
            case OLDEST:
                return gameRepository
                        .findGamesByUser_IdAndPublishedIsTrueOrderByDate(
                                userId, PageRequest.of(page, size));
            case NEWEST:
                return gameRepository
                        .findGamesByUser_IdAndPublishedIsTrueOrderByDateDesc(
                                userId, PageRequest.of(page, size));
            case RATING:
                return gameRepository
                        .findGamesByUser_IdAndPublishedIsTrueOrderByRatingDesc(
                                userId, PageRequest.of(page, size));
            default:
                throw new RuntimeException("Unknown enum value: " +
                        sortingMode);
        }
    }

    /**
     * Returns a {@link Page} with {@link Game}s favorited by a certain
     * {@link User}.
     *
     * @param user        the {@link User} whose favorited {@link Game}s
     *                    should be returned.
     * @param sortingMode a {@link SortingMode} of the {@link Game}s.
     * @param page        the {@link Page} number.
     * @param size        the {@link Page} size.
     * @return a {@link Page} with the {@link Game}s favorited by the given
     * {@link User} according to the given {@link Page} number and {@link Page}
     * size.
     * @throws IllegalPageValueException if the {@link Page} number is less
     *                                   than 0.
     * @see SortingMode
     */
    public Page<Game> getUserFavoritedGamesPage(int page, int size,
                                                @NonNull SortingMode sortingMode,
                                                @NonNull User user)
            throws IllegalPageValueException {
        if (page < 0) {
            throw new IllegalPageValueException("Page value cannot be" +
                    " less than 0!");
        }

        switch (sortingMode) {
            case OLDEST:
                return gameRepository
                        .findAllByFavoritedIsContainingAndPublishedIsTrueOrderByDate(
                                user, PageRequest.of(page, size));
            case NEWEST:
                return gameRepository
                        .findAllByFavoritedIsContainingAndPublishedIsTrueOrderByDateDesc(
                                user, PageRequest.of(page, size));
            case RATING:
                return gameRepository
                        .findAllByFavoritedIsContainingAndPublishedIsTrueOrderByRatingDesc(
                                user, PageRequest.of(page, size));
            default:
                throw new RuntimeException("Unknown enum value: " +
                        sortingMode);
        }
    }

    /**
     * Returns a {@link Page} with unpublished {@link Game}s created by
     * a certain {@link User}.
     *
     * @param user the {@link User} which unpublished {@link Game}s should be
     *             returned.
     * @param page the {@link Page} number.
     * @param size the {@link Page} size.
     * @return a {@link Page} with unpublished {@link Game}s created by the given
     * {@link User} according to the given {@link Page} number and {@link Page}
     * size.
     * @throws IllegalPageValueException if the {@link Page} number is less
     *                                   than 0.
     */
    public Page<Game> getUserNotPublishedGamesPage(int page, int size,
                                                   @NonNull User user)
            throws IllegalPageValueException {
        if (page < 0) {
            throw new IllegalPageValueException("Page value cannot be" +
                    " less than 0!");
        }

        return gameRepository.findGamesByUserAndPublishedIsFalseOrderByDate(user,
                PageRequest.of(page, size));
    }

    private void deleteGame(Game game) {
        gameRepository.delete(game);
    }

    /**
     * An enumeration that represents a sorting mode of the {@link Game}s.
     */
    public enum SortingMode {
        /**
         * Sort {@link Game}s from oldest to newest.
         */
        OLDEST,
        /**
         * Sort {@link Game}s from newest to oldest.
         */
        NEWEST,
        /**
         * Sort {@link Game}s by rating.
         */
        RATING
    }

    /**
     * An exception indicating that a certain {@link Game} has not been
     * found.
     */
    public static class GameNotFoundException extends Exception {
        /**
         * Instantiates a new Game Not Found Exception.
         *
         * @param s the message that indicates the cause of the exception.
         */
        public GameNotFoundException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a certain {@link GameNode} has not
     * been found.
     */
    public static class NodeNotFoundException extends Exception {
        /**
         * Instantiates a new Node Not Found Exception.
         *
         * @param t the cause of the exception.
         */
        public NodeNotFoundException(Throwable t) {
            super(t);
        }
    }

    /**
     * An exception indicating that some verification errors has been
     * occurred while processing a {@link Game} entity.
     */
    public static class VerificationException extends Exception {
        /**
         * Instantiates a new Verification Exception.
         *
         * @param t the cause of the exception.
         */
        public VerificationException(Throwable t) {
            super(t);
        }

        /**
         * Instantiates a new Verification Exception.
         *
         * @param s the message that indicates the cause of the exception.
         */
        public VerificationException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that some sort of forbidden manipulation
     * attempt has been made to a {@link Game} entity.
     */
    public static class ForbiddenException extends Exception {
        /**
         * Instantiates a new Forbidden Exception.
         *
         * @param t the cause of the exception.
         */
        public ForbiddenException(Throwable t) {
            super(t);
        }

        /**
         * Instantiates a new Forbidden Exception.
         *
         * @param s the message that indicates the cause of the exception.
         */
        public ForbiddenException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a certain {@link Comment} has not been
     * found.
     */
    public static class CommentNotFoundException extends Exception {
        /**
         * Instantiates a new Comment Not Found Exception.
         *
         * @param t the cause of the exception.
         */
        public CommentNotFoundException(Throwable t) {
            super(t);
        }
    }

    /**
     * An exception indicating that a certain {@link Review} has not been found.
     */
    public static class ReviewNotFoundException extends Exception {
        /**
         * Instantiates a new Review Not Found Exception.
         *
         * @param t the cause of the exception.
         */
        public ReviewNotFoundException(Throwable t) {
            super(t);
        }
    }

    /**
     * An exception indicating that a certain {@link Page} value is
     * wrong.
     */
    public static class IllegalPageValueException extends Exception {
        /**
         * Instantiates a new Illegal Page Value Exception.
         *
         * @param s the message that indicates the cause of the exception.
         */
        public IllegalPageValueException(String s) {
            super(s);
        }
    }
}

