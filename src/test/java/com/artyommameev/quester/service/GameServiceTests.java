package com.artyommameev.quester.service;

import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.Review;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.repository.GameRepository;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
@PrepareForTest({GameService.class, Review.Rating.class})
@SuppressWarnings("ConstantConditions")
public class GameServiceTests {

    private GameService gameService;

    @Mock
    private Game game;
    @Mock
    private User user;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private Page<Game> gamePage;

    @Before
    public void setUp() {
        gameService = new GameService(gameRepository);
    }

    @Test
    public void getGameReturnsGameIfPresent() throws Exception {
        when(game.getId()).thenReturn(1L);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        assertSame(game, gameService.getGame(1L));
    }

    @Test(expected = GameService.GameNotFoundException.class)
    public void getGameThrowsGameNotFoundExceptionIfNotPresent() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        gameService.getGame(1L);
    }

    @Test(expected = NullPointerException.class)
    public void saveGameThrowsNullPointerExceptionIfGameIsNull() {
        gameService.saveGame(null);
    }

    @Test
    public void saveGameSavesGame() {
        when(gameRepository.save(any())).thenReturn(game);

        Assert.assertEquals(game, gameService.saveGame(game));
    }

    @Test(expected = NullPointerException.class)
    public void getPublishedGamesPageThrowsNullPointerExceptionIfSortingModeIsNull() throws Exception {
        gameService.getPublishedGamesPage(1, 2, null);
    }

    @Test
    public void getPublishedGamesPageThrowsIllegalPageValueExceptionIfPageLessThanZero() {
        assertThrows(GameService.IllegalPageValueException.class,
                () -> gameService.getPublishedGamesPage(
                        -1, 2, GameService.SortingMode.OLDEST));

        assertThrows(GameService.IllegalPageValueException.class,
                () -> gameService.getPublishedGamesPage(
                        Integer.MIN_VALUE, 2,
                        GameService.SortingMode.OLDEST));
    }

    @Test
    public void getPublishedGamesPageGetsPublishedGamesPageSortedByOldest() throws Exception {
        when(gameRepository.findGamesByPublishedIsTrueOrderByDate(
                PageRequest.of(0, 2)))
                .thenReturn(gamePage);

        assertSame(gamePage, gameService.getPublishedGamesPage(
                0, 2, GameService.SortingMode.OLDEST));
    }

    @Test
    public void getPublishedGamesPageGetsPublishedGamesPageSortedByNewest() throws Exception {
        when(gameRepository.findGamesByPublishedIsTrueOrderByDateDesc(
                PageRequest.of(0, 2))).thenReturn(gamePage);

        assertSame(gamePage, gameService.getPublishedGamesPage(
                0, 2, GameService.SortingMode.NEWEST));
    }

    @Test
    public void getPublishedGamesPageGetsPublishedGamesPageSortedByRating() throws Exception {
        when(gameRepository.findGamesByPublishedIsTrueOrderByRatingDesc(
                PageRequest.of(0, 2))).thenReturn(gamePage);

        assertSame(gamePage, gameService.getPublishedGamesPage(
                0, 2, GameService.SortingMode.RATING));
    }

    @Test(expected = NullPointerException.class)
    public void createGameNodeThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.createGameNode(1L, null,
                "parentId", "id", "name",
                "desc", GameNode.NodeType.ROOM,
                "flagId", GameNode.Condition.FlagState.ACTIVE);
    }

    @Test
    public void createGameNodeCreatesNode() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.createGameNode(1L, user, "parentId",
                "id", "name", "desc",
                GameNode.NodeType.ROOM, "flagId",
                GameNode.Condition.FlagState.ACTIVE);

        verify(game, times(1)).addNode("id",
                "parentId", "name", "desc",
                GameNode.NodeType.ROOM, "flagId",
                GameNode.Condition.FlagState.ACTIVE, user);
        verify(gameRepository, times(1)).save(game);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void createGameNodeThrowsForbiddenExceptionIfGameThrowsForbiddenManipulationException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        doThrow(new Game.ForbiddenManipulationException(""))
                .when(game).addNode("id", "parentId", "name",
                "desc", GameNode.NodeType.ROOM, "flagId",
                GameNode.Condition.FlagState.ACTIVE, user);

        gameService.createGameNode(1L, user, "parentId",
                "id", "name", "desc",
                GameNode.NodeType.ROOM, "flagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameService.VerificationException.class)
    public void createGameNodeThrowsVerificationExceptionIfGameThrowsNodeVerificationException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NodeVerificationException(new Throwable()))
                .when(game).addNode("id", "parentId", "name",
                "desc", GameNode.NodeType.ROOM, "flagId",
                GameNode.Condition.FlagState.ACTIVE, user);

        gameService.createGameNode(1L, user, "parentId",
                "id", "name", "desc",
                GameNode.NodeType.ROOM, "flagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameService.VerificationException.class)
    public void createGameNodeThrowsVerificationExceptionIfGameThrowsIllegalRootNodeTypeException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.IllegalRootNodeTypeException(""))
                .when(game).addNode("id", "parentId", "name",
                "desc", GameNode.NodeType.ROOM, "flagId",
                GameNode.Condition.FlagState.ACTIVE, user);

        gameService.createGameNode(1L, user, "parentId",
                "id", "name", "desc",
                GameNode.NodeType.ROOM, "flagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameService.VerificationException.class)
    public void createGameNodeThrowsVerificationExceptionIfGameThrowsNotRootNodeException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NotRootNodeException(""))
                .when(game).addNode("id", "parentId", "name",
                "desc", GameNode.NodeType.ROOM, "flagId",
                GameNode.Condition.FlagState.ACTIVE, user);

        gameService.createGameNode(1L, user, "parentId",
                "id", "name", "desc",
                GameNode.NodeType.ROOM, "flagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = NullPointerException.class)
    public void editGameNodeThrowsNullPointerExceptionIfNodeIdIsNull() throws Exception {
        gameService.editGameNode(1L, null,
                new User("testName", "test@email.com",
                        "testPassword", "testPassword",
                        new BCryptPasswordEncoder(),
                        Collections.singletonList("ROLE_USER")),
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = NullPointerException.class)
    public void editGameNodeThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.editGameNode(1L, "id", null,
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test
    public void editGameNodeEditsNode() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.editGameNode(1L, "id", user,
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE);

        verify(game, times(1)).editNode("id",
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE, user);
        verify(gameRepository, times(1)).save(game);
    }

    @Test(expected = GameService.VerificationException.class)
    public void editGameNodeThrowsVerificationExceptionIfGameThrowsNodeVerificationException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NodeVerificationException(new Throwable()))
                .when(game).editNode("id",
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE, user);

        gameService.editGameNode(1L, "id", user,
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameService.VerificationException.class)
    public void editGameNodeThrowsVerificationExceptionIfGameThrowsRootNodeNotExistsException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.RootNodeNotExistsException(""))
                .when(game).editNode("id",
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE, user);

        gameService.editGameNode(1L, "id", user,
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void editGameNodeThrowsForbiddenExceptionIfGameThrowsForbiddenManipulationException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.ForbiddenManipulationException(""))
                .when(game).editNode("id",
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE, user);

        gameService.editGameNode(1L, "id", user,
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = GameService.NodeNotFoundException.class)
    public void editGameNodeThrowsNodeNotFoundExceptionIfGameThrowsRootNodeNotFoundException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NodeNotFoundException(new Throwable()))
                .when(game).editNode("id",
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE, user);

        gameService.editGameNode(1L, "id", user,
                "name", "desc", "flagId",
                GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = NullPointerException.class)
    public void deleteGameNodeThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.deleteGameNode(1L, "id", null);
    }

    @Test
    public void deleteGameNodeDeletesNode() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.deleteGameNode(1L, "id", user);

        verify(game, times(1)).deleteNode("id",
                user);
        verify(gameRepository, times(1)).save(game);
    }

    @Test(expected = GameService.NodeNotFoundException.class)
    public void deleteGameNodeThrowsNodeNotFoundExceptionIfGameThrowsRootNodeNotFoundException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NodeNotFoundException(new Throwable()))
                .when(game).deleteNode("id", user);

        gameService.deleteGameNode(1L, "id", user);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void deleteGameNodeThrowsForbiddenExceptionIfGameThrowsForbiddenManipulationException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.ForbiddenManipulationException(new Throwable()))
                .when(game).deleteNode("id", user);

        gameService.deleteGameNode(1L, "id", user);
    }

    @Test(expected = GameService.VerificationException.class)
    public void deleteGameNodeThrowsVerificationExceptionIfGameThrowsRootNodeNotExistsException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.RootNodeNotExistsException(""))
                .when(game).deleteNode("id", user);

        gameService.deleteGameNode(1L, "id", user);
    }

    @Test(expected = GameService.VerificationException.class)
    public void deleteGameNodeThrowsVerificationExceptionIfGameThrowsNodeVerificationException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NodeVerificationException(new Exception()))
                .when(game).deleteNode("id", user);

        gameService.deleteGameNode(1L, "id", user);
    }

    @Test(expected = NullPointerException.class)
    public void createCommentThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.createComment(1L, null, "text");
    }

    @Test
    public void createCommentCreatesComment() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.createComment(1L, user, "text");

        verify(game, times(1)).addComment("text",
                user);
        verify(gameRepository, times(1)).save(game);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void createCommentThrowsForbiddenExceptionIfGameThrowsNotPublishedException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NotPublishedException(""))
                .when(game).addComment("text", user);

        gameService.createComment(1L, user, "text");
    }

    @Test(expected = GameService.VerificationException.class)
    public void createCommentThrowsVerificationExceptionIfGameThrowsEmptyStringException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.EmptyStringException(""))
                .when(game).addComment("text", user);

        gameService.createComment(1L, user, "text");
    }

    @Test(expected = GameService.VerificationException.class)
    public void createCommentThrowsVerificationExceptionIfGameThrowsNullValueException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NullValueException(""))
                .when(game).addComment("text", user);

        gameService.createComment(1L, user, "text");
    }

    @Test(expected = NullPointerException.class)
    public void editCommentThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.editComment(1L, 2L, null, "text");
    }

    @Test
    public void editCommentEditsComment() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.editComment(1L, 2L, user, "text");

        verify(game, times(1)).editComment(2L,
                "text", user);
        verify(gameRepository, times(1)).save(game);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void editCommentThrowsForbiddenExceptionIfGameThrowsNotPublishedException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NotPublishedException(""))
                .when(game).editComment(2L, "text", user);

        gameService.editComment(1L, 2L, user, "text");
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void editCommentThrowsForbiddenExceptionIfGameThrowsForbiddenManipulationException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.ForbiddenManipulationException(""))
                .when(game).editComment(2L, "text", user);

        gameService.editComment(1L, 2L, user, "text");
    }

    @Test(expected = GameService.CommentNotFoundException.class)
    public void editCommentThrowsCommentNotFoundExceptionIfGameThrowsCommentNotFoundException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.CommentNotFoundException(""))
                .when(game).editComment(2L, "text", user);

        gameService.editComment(1L, 2L, user, "text");
    }

    @Test(expected = GameService.VerificationException.class)
    public void editCommentThrowsVerificationExceptionIfGameThrowsEmptyStringException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.EmptyStringException(""))
                .when(game).editComment(2L, "text", user);

        gameService.editComment(1L, 2L, user, "text");
    }

    @Test(expected = GameService.VerificationException.class)
    public void editCommentThrowsVerificationExceptionIfGameThrowsNullValueException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NullValueException(""))
                .when(game).editComment(2L, "text", user);

        gameService.editComment(1L, 2L, user, "text");
    }

    @Test(expected = NullPointerException.class)
    public void deleteCommentThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.deleteComment(1L, 2L, null);
    }

    @Test
    public void deleteCommentDeletesComment() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.deleteComment(1L, 2L, user);

        verify(game, times(1)).deleteComment(2L,
                user);
        verify(gameRepository, times(1)).save(game);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void deleteCommentThrowsForbiddenExceptionIfGameThrowsNotPublishedException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NotPublishedException(""))
                .when(game).deleteComment(2L, user);

        gameService.deleteComment(1L, 2L, user);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void deleteCommentThrowsForbiddenExceptionIfGameThrowsForbiddenManipulationException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.ForbiddenManipulationException(""))
                .when(game).deleteComment(2L, user);

        gameService.deleteComment(1L, 2L, user);
    }

    @Test(expected = GameService.CommentNotFoundException.class)
    public void deleteCommentThrowsCommentNotFoundExceptionIfGameThrowsCommentNotFoundException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.CommentNotFoundException(""))
                .when(game).deleteComment(2L, user);

        gameService.deleteComment(1L, 2L, user);
    }

    @Test(expected = NullPointerException.class)
    public void createGameThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.createGame("name", "desc", "lang",
                null, true);
    }

    @Test
    public void createGameCreatesGame() throws Exception {
        final ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(
                Game.class);

        ReflectionTestUtils.setField(user, "id", 1);

        when(gameRepository.save(any())).thenReturn(game);

        assertSame(game, gameService.createGame("name",
                "desc", "lang", user, true));

        verify(gameRepository, times(1))
                .save(captor.capture());

        val createdGame = captor.getValue();

        assertEquals("name", createdGame.getName());
        assertEquals("desc", createdGame.getDescription());
        assertEquals("lang", createdGame.getLanguage());
        assertEquals(user, createdGame.getUser());
        assertTrue(createdGame.isPublished());
    }

    @Test(expected = GameService.VerificationException.class)
    public void createGameThrowsVerificationExceptionIfGameThrowsEmptyStringException() throws Exception {
        PowerMockito.whenNew(Game.class).withAnyArguments()
                .thenThrow(new Game.EmptyStringException(""));

        gameService.createGame("name", "desc", "lang",
                new User("testName", "test@email.com",
                        "testPassword", "testPassword",
                        new BCryptPasswordEncoder(),
                        Collections.singletonList("ROLE_USER")), true);
    }

    @Test(expected = GameService.VerificationException.class)
    public void createGameThrowsVerificationExceptionIfGameThrowsNullValueException() throws Exception {
        PowerMockito.whenNew(Game.class).withAnyArguments()
                .thenThrow(new Game.NullValueException(""));

        gameService.createGame("name", "desc", "lang",
                new User("testName", "test@email.com",
                        "testPassword", "testPassword",
                        new BCryptPasswordEncoder(),
                        Collections.singletonList("ROLE_USER")), true);
    }

    @Test(expected = NullPointerException.class)
    public void editGameThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.editGame(1L, "name", "desc",
                "lang", null, true);
    }

    @Test
    public void editGameEditsGame() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.editGame(1L, "name", "desc",
                "lang", user, true);

        verify(game, times(1))
                .updateName("name", user);
        verify(game, times(1))
                .updateDescription("desc", user);
        verify(game, times(1))
                .updateLanguage("lang", user);
        verify(game, times(1))
                .updatePublished(true, user);
        verify(gameRepository, times(1)).save(game);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void editGameThrowsForbiddenExceptionIfGameThrowsForbiddenManipulationException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.ForbiddenManipulationException(""))
                .when(game).updateName("name", user);

        gameService.editGame(1L, "name", "desc",
                "lang", user, true);
    }

    @Test(expected = GameService.VerificationException.class)
    public void editGameThrowsVerificationExceptionIfGameThrowsEmptyStringException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.EmptyStringException(""))
                .when(game).updateName("name", user);

        gameService.editGame(1L, "name", "desc",
                "lang", user, true);
    }

    @Test(expected = GameService.VerificationException.class)
    public void editGameThrowsVerificationExceptionIfGameThrowsNullValueException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NullValueException(""))
                .when(game).updateName("name", user);

        gameService.editGame(1L, "name", "desc",
                "lang", user, true);
    }

    @Test(expected = NullPointerException.class)
    public void deleteGameThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.deleteGame(1L, null);
    }

    @Test
    public void deleteGameDeletesGame() throws Exception {
        when(game.canBeModifiedFrom(any())).thenReturn(true);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.deleteGame(1L, new User("testName", "test@email.com",
                "testPassword", "testPassword",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER")));

        verify(gameRepository, times(1)).delete(game);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void deleteGameThrowsForbiddenExceptionIfGameCanNotBeModifiedFromThatUser() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.deleteGame(1L, user);
    }

    @Test(expected = NullPointerException.class)
    public void createReviewThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.createReview(1L, "4", null);
    }

    @Test
    public void createReviewCreatesReview() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.createReview(1L, "5", user);

        verify(game, times(1)).addReview(Review.Rating.FIVE,
                user);
        verify(gameRepository, times(1)).save(game);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void createReviewThrowsForbiddenExceptionIfGameThrowsNotPublishedException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NotPublishedException(""))
                .when(game).addReview(Review.Rating.FIVE, user);

        gameService.createReview(1L, "5", user);
    }

    @Test(expected = GameService.VerificationException.class)
    public void createReviewThrowsVerificationExceptionIfReviewThrowsNullRatingException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        try (val ignored =
                     mockStatic(Review.Rating.class)) {
            when(Review.Rating.fromString(anyString()))
                    .thenThrow(new Review.NullRatingException(""));

            gameService.createReview(1L, "5", user);
        }
    }

    @Test(expected = GameService.VerificationException.class)
    public void createReviewThrowsVerificationExceptionIfReviewThrowsNotNumberRatingException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        try (val ignored =
                     mockStatic(Review.Rating.class)) {
            when(Review.Rating.fromString(anyString()))
                    .thenThrow(new Review.NotNumberRatingException(""));

            gameService.createReview(1L, "5", user);
        }
    }

    @Test(expected = GameService.VerificationException.class)
    public void createReviewThrowsVerificationExceptionIfReviewThrowsWrongRatingException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        try (val ignored =
                     mockStatic(Review.Rating.class)) {
            when(Review.Rating.fromString(anyString()))
                    .thenThrow(new Review.WrongRatingException(""));

            gameService.createReview(1L, "5", user);
        }
    }

    @Test(expected = NullPointerException.class)
    public void editReviewThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.editReview(1L, "4", null);
    }

    @Test
    public void editReviewEditsReview() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.editReview(1L, "5", user);

        verify(game, times(1)).editReview(
                Review.Rating.FIVE, user);
        verify(gameRepository, times(1)).save(game);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void editReviewThrowsForbiddenExceptionIfGameThrowsNotPublishedException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NotPublishedException(""))
                .when(game).editReview(Review.Rating.FIVE, user);

        gameService.editReview(1L, "5", user);
    }

    @Test(expected = GameService.ReviewNotFoundException.class)
    public void editReviewThrowsReviewNotFoundExceptionIfGameThrowsReviewNotFoundException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.ReviewNotFoundException(""))
                .when(game).editReview(Review.Rating.FIVE, user);

        gameService.editReview(1L, "5", user);
    }

    @Test(expected = GameService.VerificationException.class)
    public void editReviewThrowsVerificationExceptionIfReviewThrowsNullRatingException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        try (val ignored =
                     mockStatic(Review.Rating.class)) {
            when(Review.Rating.fromString(anyString()))
                    .thenThrow(new Review.NullRatingException(""));

            gameService.editReview(1L, "5", user);
        }
    }

    @Test(expected = GameService.VerificationException.class)
    public void editReviewThrowsVerificationExceptionIfReviewThrowsNotNumberRatingException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        try (val ignored =
                     mockStatic(Review.Rating.class)) {
            when(Review.Rating.fromString(anyString()))
                    .thenThrow(new Review.NotNumberRatingException(""));

            gameService.editReview(1L, "5", user);
        }
    }

    @Test(expected = GameService.VerificationException.class)
    public void editReviewThrowsVerificationExceptionIfReviewThrowsWrongRatingException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        try (val ignored =
                     mockStatic(Review.Rating.class)) {
            when(Review.Rating.fromString(anyString()))
                    .thenThrow(new Review.WrongRatingException(""));

            gameService.editReview(1L, "5", user);
        }
    }

    @Test(expected = NullPointerException.class)
    public void makeFavoritedThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.makeFavorited(1L, null);
    }

    @Test
    public void makeFavoritedMakesFavorited() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.makeFavorited(1L, user);

        verify(game, times(1)).addFavoritedUser(user);
        verify(gameRepository, times(1)).save(game);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void makeFavoritedThrowsForbiddenExceptionIfGameThrowsNotPublishedException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        doThrow(new Game.NotPublishedException(""))
                .when(game).addFavoritedUser(user);

        gameService.makeFavorited(1L, user);
    }

    @Test(expected = NullPointerException.class)
    public void makeUnfavoritedThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.makeUnfavorited(1L, null);
    }

    @Test
    public void makeUnfavoritedMakesUnfavorited() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.makeUnfavorited(1L, user);

        verify(game, times(1)).removeFavoritedUser(user);
        verify(gameRepository, times(1)).save(game);
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void makeUnfavoritedThrowsForbiddenExceptionIfGameThrowsNotPublishedException() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        doThrow(new Game.NotPublishedException(""))
                .when(game).removeFavoritedUser(user);

        gameService.makeUnfavorited(1L, user);
    }

    @Test(expected = NullPointerException.class)
    public void getGameForEditThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.getGameForEdit(1L, null);
    }

    @Test
    public void getGameForEditGetsGameForEdit() throws Exception {
        when(game.canBeModifiedFrom(any())).thenReturn(true);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        assertSame(game, gameService.getGameForEdit(1L, user));
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void getGameForEditThrowsForbiddenExceptionIfGameCanNotBeModifiedFromThatUser() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.getGameForEdit(1L, user);
    }

    @Test
    public void getGameJsonGetsJson() throws Exception {
        val jsonReadyGame = mock(Game.JsonReadyGame.class);

        when(game.getJsonReadyGame()).thenReturn(jsonReadyGame);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        assertSame(jsonReadyGame, gameService.getGameJson(1L));
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void getGameJsonThrowsForbiddenExceptionIfGameThrowsNotPublishedException() throws Exception {
        when(game.getJsonReadyGame())
                .thenThrow(new Game.NotPublishedException(""));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.getGameJson(1L);
    }

    @Test
    public void getGameForViewGetsGameForViewFromUser() throws Exception {
        when(game.canBeViewedFrom(any())).thenReturn(true);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        assertSame(game, gameService.getGameForView(1L, user));
    }

    @Test
    public void getGameForViewGetsGameForViewFromNullUser() throws Exception {
        when(game.canBeViewedFrom(any())).thenReturn(true);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        assertSame(game, gameService.getGameForView(1L, null));
    }

    @Test(expected = GameService.ForbiddenException.class)
    public void getGameForViewThrowsForbiddenExceptionIfGameCanNotBeViewedFromThatUser() throws Exception {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        gameService.getGameForView(1L, user);
    }

    @Test(expected = NullPointerException.class)
    public void getUserPublishedGamesPageThrowsNullPointerExceptionIfSoringModeIsNull() throws Exception {
        gameService.getUserPublishedGamesPage(1, 2, null,
                2L);
    }

    @Test
    public void getUserPublishedGamesPageThrowsIllegalPageValueExceptionIfPageLessThanZero() {
        assertThrows(GameService.IllegalPageValueException.class,
                () -> gameService.getUserPublishedGamesPage(
                        -1, 2,
                        GameService.SortingMode.OLDEST, 1L));

        assertThrows(GameService.IllegalPageValueException.class,
                () -> gameService.getUserPublishedGamesPage(
                        Integer.MIN_VALUE, 2,
                        GameService.SortingMode.OLDEST, 1L));
    }

    @Test
    public void getUserPublishedGamesPageGetsUserPublishedGamesPageSortedByOldest() throws Exception {
        when(gameRepository
                .findGamesByUser_IdAndPublishedIsTrueOrderByDate(
                        1L, PageRequest.of(0, 2)))
                .thenReturn(gamePage);

        assertSame(gamePage, gameService.getUserPublishedGamesPage(
                0, 2, GameService.SortingMode.OLDEST, 1L));
    }

    @Test
    public void getUserPublishedGamesPageGetUserPublishedGamesPageSortedByNewest() throws Exception {
        when(gameRepository
                .findGamesByUser_IdAndPublishedIsTrueOrderByDateDesc(
                        1L, PageRequest.of(0, 2)))
                .thenReturn(gamePage);

        assertSame(gamePage, gameService.getUserPublishedGamesPage(
                0, 2, GameService.SortingMode.NEWEST, 1L));
    }

    @Test
    public void getUserPublishedGamesPageGetsUserPublishedGamesPageSortedByRating() throws Exception {
        when(gameRepository
                .findGamesByUser_IdAndPublishedIsTrueOrderByRatingDesc(
                        1L, PageRequest.of(0, 2)))
                .thenReturn(gamePage);

        assertSame(gamePage, gameService.getUserPublishedGamesPage(
                0, 2, GameService.SortingMode.RATING, 1L));
    }

    @Test(expected = NullPointerException.class)
    public void getUserFavoritedGamesPageThrowsNullPointerExceptionIfSortingModeIsNull() throws Exception {
        gameService.getUserFavoritedGamesPage(1, 2,
                null, user);
    }

    @Test(expected = NullPointerException.class)
    public void getUserFavoritedGamesPageThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.getUserFavoritedGamesPage(1, 2,
                GameService.SortingMode.OLDEST, null);
    }

    @Test
    public void getUserFavoritedGamesPageThrowsIllegalPageValueExceptionIfPageLessThanZero() {
        assertThrows(GameService.IllegalPageValueException.class,
                () -> gameService.getUserFavoritedGamesPage(
                        -1, 2,
                        GameService.SortingMode.OLDEST, user));

        assertThrows(GameService.IllegalPageValueException.class,
                () -> gameService.getUserFavoritedGamesPage(
                        Integer.MIN_VALUE, 2,
                        GameService.SortingMode.OLDEST, user));
    }

    @Test
    public void getUserFavoritedGamesPageGetsUserFavoritedGamesPageSortedByOldest() throws Exception {
        when(gameRepository
                .findAllByFavoritedIsContainingAndPublishedIsTrueOrderByDate(
                        user, PageRequest.of(0, 2)))
                .thenReturn(gamePage);

        assertSame(gamePage, gameService.getUserFavoritedGamesPage(
                0, 2, GameService.SortingMode.OLDEST, user));
    }

    @Test
    public void getUserFavoritedGamesPageGetUserFavoritedGamesPageSortedByNewest() throws Exception {
        when(gameRepository
                .findAllByFavoritedIsContainingAndPublishedIsTrueOrderByDateDesc(
                        user, PageRequest.of(0, 2)))
                .thenReturn(gamePage);

        assertSame(gamePage, gameService.getUserFavoritedGamesPage(
                0, 2, GameService.SortingMode.NEWEST, user));
    }

    @Test
    public void getUserFavoritedGamesPageGetsUserFavoritedGamesPageSortedByRating() throws Exception {
        when(gameRepository
                .findAllByFavoritedIsContainingAndPublishedIsTrueOrderByRatingDesc(
                        user, PageRequest.of(0, 2)))
                .thenReturn(gamePage);

        assertSame(gamePage, gameService.getUserFavoritedGamesPage(
                0, 2, GameService.SortingMode.RATING, user));
    }

    @Test(expected = NullPointerException.class)
    public void getUserNotPublishedGamesPageThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        gameService.getUserNotPublishedGamesPage(1, 2,
                null);
    }

    @Test
    public void getUserNotPublishedGamesPageThrowsIllegalPageValueExceptionIfPageLessThanZero() {
        assertThrows(GameService.IllegalPageValueException.class,
                () -> gameService.getUserNotPublishedGamesPage(
                        -1, 2, user));

        assertThrows(GameService.IllegalPageValueException.class,
                () -> gameService.getUserNotPublishedGamesPage(
                        Integer.MIN_VALUE, 2, user));
    }

    @Test
    public void getUserNotPublishedGamesPageGetsUserNotPublishedGamesPage() throws Exception {
        when(gameRepository
                .findGamesByUserAndPublishedIsFalseOrderByDate(
                        user, PageRequest.of(0, 2)))
                .thenReturn(gamePage);

        assertSame(gamePage, gameService.getUserNotPublishedGamesPage(
                0, 2, user));
    }
}
