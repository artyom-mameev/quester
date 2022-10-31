package com.artyommameev.quester.entity;

import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.entity.gamenode.RoomNode;
import lombok.val;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.Silent.class)
@PrepareForTest(Game.class)
@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
public class GameTests {

    private Game publishedGame;
    private Game unpublishedGame;

    private User gameAuthorUser;
    private User notGameAuthorUser;
    private User adminUser;

    @Mock
    private Comment comment;

    @Before
    public void setUp() throws Exception {
        gameAuthorUser = createSimpleUser(1L);
        notGameAuthorUser = createSimpleUser(2L);
        adminUser = createAdminUser();
        publishedGame = createPublishedGame();
        unpublishedGame = createUnpublishedGame();

        when(comment.getId()).thenReturn(1L);
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        new Game("name", "description",
                "language", null, true);
    }

    @Test
    public void constructorTrimsName() throws Exception {
        val game = new Game(" name ", "description",
                "language", new User(), true);

        assertEquals("name", game.getName());
    }

    @Test(expected = Game.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfTrimmedNameIsEmpty() throws Exception {
        new Game(" ", "description", "language",
                new User(), true);
    }

    @Test(expected = Game.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfNameIsNull() throws Exception {
        new Game(null, "description", "language",
                new User(), true);
    }

    @Test
    public void constructorTrimsDescription() throws Exception {
        val game = new Game("name", " description ",
                "language", new User(), true);

        assertEquals("description", game.getDescription());
    }

    @Test(expected = Game.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfTrimmedDescriptionIsEmpty() throws Exception {
        new Game("name", " ", "language",
                new User(), true);
    }

    @Test(expected = Game.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfDescriptionIsNull() throws Exception {
        new Game("name", null, "language", new User(),
                true);
    }

    @Test
    public void constructorTrimsLanguage() throws Exception {
        val game = new Game("name", "description",
                " language ", new User(), true);

        assertEquals("language", game.getLanguage());
    }

    @Test(expected = Game.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfTrimmedLanguageIsEmpty() throws Exception {
        new Game("name", "desc", " ", new User(),
                true);
    }

    @Test(expected = Game.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfLanguageIsNull() throws Exception {
        new Game("name", "desc", null, new User(),
                true);
    }

    @Test
    public void constructorSetsDateIfGameIsPublished() {
        assertNotNull(publishedGame.getDate());
    }

    @Test
    public void constructorDontSetDateIfGameIsNotPublished() {
        assertNull(unpublishedGame.getDate());
    }

    @Test
    public void getDateReturnsNewInstanceOfDate() {
        assertNotSame(publishedGame.getDate(), publishedGame.getDate());
    }

    @Test
    public void isFavoritedByReturnsFalseIfNotFavorited() {
        assertFalse(publishedGame.isFavoritedBy(notGameAuthorUser));
    }

    @Test
    public void isFavoritedByReturnsTrueIfFavorited() throws Exception {
        publishedGame.addFavoritedUser(notGameAuthorUser);

        assertTrue(publishedGame.isFavoritedBy(notGameAuthorUser));
    }

    @Test(expected = NullPointerException.class)
    public void isFavoritedByThrowsNullPointerExceptionIfUserIsNull() {
        publishedGame.isFavoritedBy(null);
    }

    @Test
    public void getCommentsReturnsUnmodifiableList() {
        val comments = publishedGame.getComments();

        assertThrows(UnsupportedOperationException.class, () ->
                comments.add(new Comment()));

        assertThrows(UnsupportedOperationException.class, () ->
                comments.remove(new Comment()));

        assertThrows(UnsupportedOperationException.class, comments::clear);
    }

    @Test
    public void getCommentsCountReturnsCommentsCount() throws Exception {
        assertEquals(0, publishedGame.getCommentsCount());

        publishedGame.addComment("text", new User());

        assertEquals(1, publishedGame.getCommentsCount());
    }

    @Test
    public void getReviewForReturnsNullIfUserReviewNotFound() {
        assertNull(publishedGame.getReviewFor(notGameAuthorUser));
    }

    @Test
    public void getReviewForReturnsUserReview() throws Exception {
        publishedGame.addReview(Review.Rating.FIVE, notGameAuthorUser);

        assertEquals(5, publishedGame.getReviewFor(notGameAuthorUser)
                .getRating());
    }

    @Test(expected = NullPointerException.class)
    public void getReviewForThrowsNullPointerExceptionIfUserIsNull() {
        publishedGame.getReviewFor(null);
    }

    @Test(expected = Game.NotPublishedException.class)
    public void addCommentThrowsNotPublishedExceptionIfGameIsNotPublished() throws Exception {
        unpublishedGame.addComment("text", new User());
    }

    @Test
    public void addCommentAddsComment() throws Exception {
        publishedGame.addComment("text", new User());

        assertEquals("text", publishedGame.getComments().get(0).getText());
    }

    @Test(expected = NullPointerException.class)
    public void addCommentThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.addComment("text", null);
    }

    @Test(expected = Game.EmptyStringException.class)
    public void addCommentThrowsEmptyStringExceptionIfNewCommentThrowsEmptyTextException() throws Exception {
        PowerMockito.whenNew(Comment.class).withAnyArguments()
                .thenThrow(new Comment.EmptyTextException(""));

        publishedGame.addComment("text", new User());
    }

    @Test(expected = Game.NullValueException.class)
    public void addCommentThrowsNullValueExceptionIfNewCommentThrowsNullValueException() throws Exception {
        PowerMockito.whenNew(Comment.class).withAnyArguments()
                .thenThrow(new Comment.NullValueException(""));

        publishedGame.addComment("text", new User());
    }

    @Test(expected = NullPointerException.class)
    public void editCommentThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.editComment(1L, "text", null);
    }

    @Test(expected = Game.NotPublishedException.class)
    public void editCommentThrowsNotPublishedExceptionIfGameIsNotPublished() throws Exception {
        unpublishedGame.editComment(1L, "text", new User());
    }

    @Test(expected = Game.CommentNotFoundException.class)
    public void editCommentThrowsCommentNotFoundExceptionIfCommentIsNotFound() throws Exception {
        publishedGame.editComment(1L, "text", new User());
    }

    @Test(expected = Game.ForbiddenManipulationException.class)
    public void editCommentThrowsForbiddenManipulationExceptionIfEditingCommentThrowsForbiddenManipulationException() throws Exception {
        doThrow(Comment.ForbiddenManipulationException.class)
                .when(comment).editText(anyString(), any());

        ReflectionTestUtils.setField(publishedGame, "comments",
                Collections.singletonList(comment));

        publishedGame.editComment(1L, "text", gameAuthorUser);
    }

    @Test(expected = Game.NullValueException.class)
    public void editCommentThrowsNullValueExceptionIfEditingCommentThrowsNullValueException() throws Exception {
        doThrow(Comment.NullValueException.class)
                .when(comment).editText(anyString(), any());

        ReflectionTestUtils.setField(publishedGame, "comments",
                Collections.singletonList(comment));

        publishedGame.editComment(1L, "text", gameAuthorUser);
    }

    @Test(expected = Game.EmptyStringException.class)
    public void editCommentThrowsEmptyStringExceptionIfEditingCommentThrowsEmptyTextException() throws Exception {
        doThrow(Comment.EmptyTextException.class)
                .when(comment).editText(anyString(), any());

        ReflectionTestUtils.setField(publishedGame, "comments",
                Collections.singletonList(comment));

        publishedGame.editComment(1L, "text", gameAuthorUser);
    }

    @Test
    public void editCommentEditsComment() throws Exception {
        ReflectionTestUtils.setField(publishedGame, "comments",
                Collections.singletonList(comment));

        publishedGame.editComment(1L, "text2", gameAuthorUser);

        verify(comment, times(1))
                .editText("text2", gameAuthorUser);
    }

    @Test(expected = NullPointerException.class)
    public void deleteCommentThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.deleteComment(1L, null);
    }

    @Test(expected = Game.NotPublishedException.class)
    public void deleteCommentThrowsNotPublishedExceptionIfGameIsNotPublished() throws Exception {
        unpublishedGame.deleteComment(1L, gameAuthorUser);
    }

    @Test(expected = Game.CommentNotFoundException.class)
    public void deleteCommentThrowsCommentNotFoundExceptionIfCommentIsNotFound() throws Exception {
        publishedGame.deleteComment(1L, new User());
    }

    @Test(expected = Game.ForbiddenManipulationException.class)
    public void deleteCommentThrowsForbiddenManipulationExceptionIfUserCanNotModifyDeletingComment() throws Exception {
        when(comment.getUser()).thenReturn(notGameAuthorUser);

        ReflectionTestUtils.setField(publishedGame, "comments",
                Collections.singletonList(comment));

        publishedGame.deleteComment(1L, gameAuthorUser);
    }

    @Test
    public void deleteCommentDeletesComment() throws Exception {
        when(comment.canBeModifiedBy(any())).thenReturn(true);

        ReflectionTestUtils.setField(publishedGame, "comments",
                new ArrayList<>(Collections.singletonList(comment)));

        assertEquals(1, publishedGame.getComments().size());

        publishedGame.deleteComment(1L, gameAuthorUser);

        assertEquals(0, publishedGame.getComments().size());
    }

    @Test(expected = NullPointerException.class)
    public void addReviewThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.addReview(Review.Rating.FOUR, null);
    }

    @Test(expected = Game.NotPublishedException.class)
    public void addReviewThrowsNotPublishedExceptionIfGameIsNotPublished() throws Exception {
        unpublishedGame.addReview(Review.Rating.FIVE, new User());

    }

    @Test(expected = NullPointerException.class)
    public void editReviewThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.editReview(Review.Rating.FOUR, null);
    }

    @Test(expected = Game.NotPublishedException.class)
    public void editReviewThrowsNotPublishedExceptionIfGameIsNotPublished() throws Exception {
        unpublishedGame.editReview(Review.Rating.FIVE, new User());
    }

    @Test(expected = Game.ReviewNotFoundException.class)
    public void editReviewThrowsReviewNotFoundExceptionIfReviewIsNotFound() throws Exception {
        publishedGame.editReview(Review.Rating.FIVE, new User());
    }

    @Test
    public void editReviewEditsReview() throws Exception {
        publishedGame.addReview(Review.Rating.FIVE, notGameAuthorUser);

        publishedGame.editReview(Review.Rating.FOUR, notGameAuthorUser);

        assertEquals(4, publishedGame.getReviewFor(notGameAuthorUser)
                .getRating());
    }

    @Test(expected = NullPointerException.class)
    public void addFavoritedUserThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.addFavoritedUser(null);
    }

    @Test(expected = Game.NotPublishedException.class)
    public void addFavoritedUserThrowsNotPublishedExceptionIfGameIsNotPublished() throws Exception {
        unpublishedGame.addFavoritedUser(new User());
    }

    @Test
    public void addFavoritedUserAddsFavoritedUser() throws Exception {
        assertFalse(publishedGame.isFavoritedBy(notGameAuthorUser));

        publishedGame.addFavoritedUser(notGameAuthorUser);

        assertTrue(publishedGame.isFavoritedBy(notGameAuthorUser));
    }

    @Test(expected = NullPointerException.class)
    public void removeFavoritedUserThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.removeFavoritedUser(null);
    }

    @Test(expected = Game.NotPublishedException.class)
    public void removeFavoritedUserThrowsNotPublishedExceptionIfGameIsNotPublished() throws Exception {
        unpublishedGame.removeFavoritedUser(new User());
    }

    @Test
    public void removeFavoritedUserRemovesFavoritedUser() throws Exception {
        publishedGame.addFavoritedUser(notGameAuthorUser);

        assertTrue(publishedGame.isFavoritedBy(notGameAuthorUser));

        publishedGame.removeFavoritedUser(notGameAuthorUser);

        assertFalse(publishedGame.isFavoritedBy(notGameAuthorUser));
    }

    @Test(expected = NullPointerException.class)
    public void updateNameThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.updateName("name", null);
    }

    @Test(expected = Game.ForbiddenManipulationException.class)
    public void updateNameThrowsForbiddenManipulationExceptionIfUserCanNotModifyThatGame() throws Exception {
        publishedGame.updateName("name", notGameAuthorUser);
    }

    @Test(expected = Game.EmptyStringException.class)
    public void updateNameThrowsEmptyStringExceptionIfTrimmedNameIsEmpty() throws Exception {
        publishedGame.updateName(" ", gameAuthorUser);
    }

    @Test(expected = Game.NullValueException.class)
    public void updateNameThrowsNullValueExceptionIfNameIsNull() throws Exception {
        publishedGame.updateName(null, gameAuthorUser);
    }

    @Test
    public void updateNameSetsAndTrimsNameFromUser() throws Exception {
        publishedGame.updateName(" newName ", gameAuthorUser);

        assertEquals("newName", publishedGame.getName());
    }

    @Test
    public void updateNameSetsAndTrimsNameFromAdmin() throws Exception {
        publishedGame.updateName(" newName2 ", adminUser);

        assertEquals("newName2", publishedGame.getName());
    }

    @Test(expected = NullPointerException.class)
    public void updateDescriptionThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.updateDescription("desc", null);
    }

    @Test(expected = Game.ForbiddenManipulationException.class)
    public void updateDescriptionThrowsForbiddenManipulationExceptionIfUserCanNotModifyThatGame() throws Exception {
        publishedGame.updateDescription("desc", notGameAuthorUser);
    }

    @Test(expected = Game.EmptyStringException.class)
    public void updateDescriptionThrowsEmptyStringExceptionIfTrimmedDescriptionIsEmpty() throws Exception {
        publishedGame.updateDescription(" ", gameAuthorUser);
    }

    @Test(expected = Game.NullValueException.class)
    public void updateDescriptionThrowsNullValueExceptionIfDescriptionIsNull() throws Exception {
        publishedGame.updateDescription(null, gameAuthorUser);
    }

    @Test
    public void updateDescriptionSetsAndTrimsDescriptionFromUser() throws Exception {
        publishedGame.updateDescription(" newDesc ", gameAuthorUser);

        assertEquals("newDesc", publishedGame.getDescription());
    }

    @Test
    public void updateDescriptionSetsAndTrimsDescriptionFromAdmin() throws Exception {
        publishedGame.updateDescription(" newDesc2 ", adminUser);

        assertEquals("newDesc2", publishedGame.getDescription());
    }

    @Test(expected = NullPointerException.class)
    public void updateLanguageThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.updateLanguage("lang", null);
    }

    @Test(expected = Game.ForbiddenManipulationException.class)
    public void updateLanguageThrowsForbiddenManipulationExceptionIfUserCanNotModifyThatGame() throws Exception {
        publishedGame.updateLanguage("lang", notGameAuthorUser);
    }

    @Test(expected = Game.EmptyStringException.class)
    public void updateLanguageThrowsEmptyStringExceptionIfTrimmedLanguageIsEmpty() throws Exception {
        publishedGame.updateLanguage(" ", gameAuthorUser);
    }

    @Test(expected = Game.NullValueException.class)
    public void updateLanguageThrowsNullValueExceptionIfLanguageIsNull() throws Exception {
        publishedGame.updateLanguage(null, gameAuthorUser);
    }

    @Test
    public void updateLanguageSetsAndTrimsLanguageFromUser() throws Exception {
        publishedGame.updateLanguage(" newLang ", gameAuthorUser);

        assertEquals("newLang", publishedGame.getLanguage());
    }

    @Test
    public void updateLanguageSetsAndTrimsLanguageFromAdmin() throws Exception {
        publishedGame.updateLanguage(" newLang2 ", adminUser);

        assertEquals("newLang2", publishedGame.getLanguage());
    }

    @Test(expected = NullPointerException.class)
    public void updatePublishedThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.updatePublished(false, null);
    }

    @Test(expected = Game.ForbiddenManipulationException.class)
    public void updatePublishedThrowsForbiddenManipulationExceptionIfUserCanNotModifyThatGame() throws Exception {
        publishedGame.updatePublished(false, notGameAuthorUser);
    }

    @Test
    public void updatePublishedSetsDateIfGameWasNotPublishedAndSetToPublished() throws Exception {
        assertNull(unpublishedGame.getDate());

        unpublishedGame.updatePublished(true, gameAuthorUser);

        assertNotNull(unpublishedGame.getDate());
    }

    @Test
    public void updatePublishedUpdatesPublishedStatus() throws Exception {
        publishedGame.updatePublished(false, gameAuthorUser);

        assertFalse(publishedGame.isPublished());

        publishedGame.updatePublished(true, gameAuthorUser);

        assertTrue(publishedGame.isPublished());
    }

    @Test(expected = Game.NotPublishedException.class)
    public void getJsonReadyGameThrowsNotPublishedExceptionIfGameIsNotPublished() throws Exception {
        unpublishedGame.getJsonReadyGame();
    }

    @Test
    public void getJsonReadyGameReturnsJsonReadyGame() throws Exception {
        createTestRootNode(publishedGame);

        val jsonReadyGame = publishedGame.getJsonReadyGame();

        assertEquals(jsonReadyGame.getId(), publishedGame.getId());
        assertEquals(jsonReadyGame.getName(), publishedGame.getName());
        assertEquals(jsonReadyGame.getDescription(),
                publishedGame.getDescription());
        assertEquals(jsonReadyGame.getLanguage(), publishedGame.getLanguage());
        assertEquals(jsonReadyGame.getDate(), publishedGame.getDate());
        assertEquals(jsonReadyGame.getRating(), publishedGame.getRating(),
                0.00001);
        assertEquals(jsonReadyGame.getUser(), publishedGame.getUser()
                .getUsername());
        assertEquals(jsonReadyGame.getRootNode(), publishedGame.getRootNode());
    }

    @Test(expected = NullPointerException.class)
    public void addNodeThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.addNode("testId", "parentId",
                "testName", "testDesc",
                GameNode.NodeType.ROOM, "testCondFlagId",
                GameNode.Condition.FlagState.ACTIVE, null);
    }

    @Test(expected = Game.ForbiddenManipulationException.class)
    public void addNodeThrowsForbiddenManipulationExceptionIfUserCanNotModifyThatGame() throws Exception {
        publishedGame.addNode("testId", "parentId",
                "testName", "testDesc",
                GameNode.NodeType.ROOM, "testCondFlagId",
                GameNode.Condition.FlagState.ACTIVE, notGameAuthorUser);
    }

    @Test
    public void addNodeAddsNodeToRootNodeIfRootNodeIsNotNull() throws Exception {
        createMockRootNode(publishedGame);

        publishedGame.addNode("testId", "parentId", "testName",
                "testDesc", GameNode.NodeType.ROOM,
                "testCondFlagId", GameNode.Condition.FlagState.ACTIVE,
                gameAuthorUser);

        verify(publishedGame.getRootNode(), times(1))
                .addNodeToChildren("testId", "parentId",
                        "testName", "testDesc",
                        GameNode.NodeType.ROOM,
                        "testCondFlagId",
                        GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void addNodeThrowsNodeVerificationExceptionIfRootNodeThrowsFlagNotExistsExceptionAndRootNodeIsNotNull() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.FlagNotExistsException(""))
                .when(publishedGame.getRootNode()).addNodeToChildren(anyString(),
                anyString(), anyString(), anyString(), any(), anyString(),
                any());

        publishedGame.addNode("testId", "parentId", "testName",
                "testDesc", GameNode.NodeType.ROOM,
                "testCondFlagId", GameNode.Condition.FlagState.ACTIVE,
                gameAuthorUser);
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void addNodeThrowsNodeVerificationExceptionIfRootNodeThrowsParentNotExistsExceptionAndRootNodeIsNotNull() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.ParentNotExistsException(""))
                .when(publishedGame.getRootNode()).addNodeToChildren(anyString(),
                anyString(), anyString(), anyString(), any(), anyString(),
                any());

        publishedGame.addNode("testId", "parentId", "testName",
                "testDesc", GameNode.NodeType.ROOM,
                "testCondFlagId", GameNode.Condition.FlagState.ACTIVE,
                gameAuthorUser);
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void addNodeThrowsNodeVerificationExceptionIfRootNodeThrowsParentMismatchExceptionAndRootNodeIsNotNull() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.ParentMismatchException(""))
                .when(publishedGame.getRootNode()).addNodeToChildren(anyString(),
                anyString(), anyString(), anyString(), any(), anyString(),
                any());

        publishedGame.addNode("testId", "parentId", "testName",
                "testDesc", GameNode.NodeType.ROOM,
                "testCondFlagId", GameNode.Condition.FlagState.ACTIVE,
                gameAuthorUser);
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void addNodeThrowsNodeVerificationExceptionIfRootNodeThrowsEmptyStringExceptionAndRootNodeIsNotNull() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.EmptyStringException(""))
                .when(publishedGame.getRootNode()).addNodeToChildren(anyString(),
                anyString(), anyString(), anyString(), any(), anyString(),
                any());

        publishedGame.addNode("testId", "parentId", "testName",
                "testDesc", GameNode.NodeType.ROOM,
                "testCondFlagId", GameNode.Condition.FlagState.ACTIVE,
                gameAuthorUser);
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void addNodeThrowsNodeVerificationExceptionIfRootNodeThrowsAlreadyExistsExceptionAndRootNodeIsNotNull() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.AlreadyExistsException(""))
                .when(publishedGame.getRootNode()).addNodeToChildren(anyString(),
                anyString(), anyString(), anyString(), any(), anyString(),
                any());

        publishedGame.addNode("testId", "parentId", "testName",
                "testDesc", GameNode.NodeType.ROOM,
                "testCondFlagId", GameNode.Condition.FlagState.ACTIVE,
                gameAuthorUser);
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void addNodeThrowsNodeVerificationExceptionIfRootNodeThrowsNullValueExceptionAndRootNodeIsNotNull() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.NullValueException(""))
                .when(publishedGame.getRootNode()).addNodeToChildren(anyString(),
                anyString(), anyString(), anyString(), any(), anyString(),
                any());

        publishedGame.addNode("testId", "parentId", "testName",
                "testDesc", GameNode.NodeType.ROOM,
                "testCondFlagId", GameNode.Condition.FlagState.ACTIVE,
                gameAuthorUser);
    }

    @Test(expected = Game.NotRootNodeException.class)
    public void addNodeThrowsNotRootNodeExceptionIfRootNodeIsNullAndParentIdIsNotIndicatingThatAddingNodeIsRootNode() throws Exception {
        publishedGame.addNode("testId", "notRootNode",
                "testName", "testDesc", GameNode.NodeType.ROOM,
                null, null, gameAuthorUser);
    }

    @Test
    public void addNodeThrowsNotRootNodeExceptionIfRootNodeIsNullAndNodeTypeIsNotRoom() {
        assertThrows(Game.IllegalRootNodeTypeException.class, () ->
                publishedGame.addNode("testId", "###",
                        "testName", null, GameNode.NodeType.CHOICE,
                        null, null, gameAuthorUser));

        assertThrows(Game.IllegalRootNodeTypeException.class, () ->
                publishedGame.addNode("testId", "###", "testName",
                        null, GameNode.NodeType.FLAG,
                        null, null, gameAuthorUser));

        assertThrows(Game.IllegalRootNodeTypeException.class, () ->
                publishedGame.addNode("testId", "###", null,
                        null, GameNode.NodeType.CONDITION,
                        "testCondFlagId",
                        GameNode.Condition.FlagState.ACTIVE, gameAuthorUser));
    }

    @Test
    public void addNodeCreatesNewRootNodeIfRootNodeWasNull() throws Exception {
        publishedGame.addNode("testId", "###", "testName",
                "testDesc", GameNode.NodeType.ROOM,
                null, null, gameAuthorUser);

        assertEquals("testId", publishedGame.getRootNode().getId());
        assertEquals("testName", publishedGame.getRootNode().getName());
        assertEquals("testDesc", publishedGame.getRootNode()
                .getDescription());
        assertEquals(GameNode.NodeType.ROOM, publishedGame.getRootNode()
                .getType());
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void addNodeThrowsNodeVerificationExceptionIfNewGameNodeThrowsEmptyStringExceptionAndRootNodeIsNull() throws Exception {
        PowerMockito.whenNew(RoomNode.class).withAnyArguments()
                .thenThrow(new GameNode.EmptyStringException(""));

        publishedGame.addNode("testId", "###", "testName",
                "testDesc", GameNode.NodeType.ROOM,
                null, null, gameAuthorUser);
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void addNodeThrowsNodeVerificationExceptionIfNewGameNodeThrowsNullValueExceptionAndRootNodeIsNull() throws Exception {
        PowerMockito.whenNew(RoomNode.class).withAnyArguments()
                .thenThrow(new GameNode.NullValueException(""));

        publishedGame.addNode("testId", "###", "testName",
                "testDesc", GameNode.NodeType.ROOM,
                null, null, gameAuthorUser);
    }

    @Test(expected = NullPointerException.class)
    public void editNodeThrowsNullPointerExceptionIfNodeIdIsNull() throws Exception {
        publishedGame.editNode(null, "testName",
                "testDesc", "testCondFlagId",
                GameNode.Condition.FlagState.ACTIVE, new User());
    }

    @Test(expected = NullPointerException.class)
    public void editNodeThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.editNode("testId", "testName",
                "testDesc", "testCondFlagId",
                GameNode.Condition.FlagState.ACTIVE, null);
    }

    @Test(expected = Game.ForbiddenManipulationException.class)
    public void editNodeThrowsForbiddenManipulationExceptionIfUserCanNotModifyThatGame() throws Exception {
        publishedGame.editNode("testId", "testName",
                "testDesc", "testCondFlagId",
                GameNode.Condition.FlagState.ACTIVE, notGameAuthorUser);
    }

    @Test(expected = Game.RootNodeNotExistsException.class)
    public void editNodeThrowsRootNodeNotExistsExceptionIfRootNodeHasNotBeenAdded() throws Exception {
        publishedGame.editNode("testId", "testName",
                "testDesc", "testCondFlagId",
                GameNode.Condition.FlagState.ACTIVE, gameAuthorUser);
    }

    @Test
    public void editNodeEditsNode() throws Exception {
        createMockRootNode(publishedGame);

        publishedGame.editNode("testId", "testName2",
                "testDesc2", "testCondFlagId2",
                GameNode.Condition.FlagState.ACTIVE, gameAuthorUser);

        verify(publishedGame.getRootNode(), times(1))
                .editNode("testId", "testName2",
                        "testDesc2", "testCondFlagId2",
                        GameNode.Condition.FlagState.ACTIVE);
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void editNodeThrowsNodeVerificationExceptionIfRootNodeThrowsFlagNotExistsException() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.FlagNotExistsException(""))
                .when(publishedGame.getRootNode()).editNode(anyString(), anyString(),
                anyString(), anyString(), any());

        publishedGame.editNode("testId", "testName",
                "testDesc", "testCondFlagId",
                GameNode.Condition.FlagState.ACTIVE, gameAuthorUser);
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void editNodeThrowsNodeVerificationExceptionIfRootNodeThrowsEmptyStringException() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.EmptyStringException(""))
                .when(publishedGame.getRootNode()).editNode(anyString(),
                anyString(), anyString(), anyString(), any());

        publishedGame.editNode("testId", "testName",
                "testDesc", "testCondFlagId",
                GameNode.Condition.FlagState.ACTIVE, gameAuthorUser);
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void editNodeThrowsNodeVerificationExceptionIfRootNodeThrowsNullValueException() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.NullValueException(""))
                .when(publishedGame.getRootNode()).editNode(anyString(),
                anyString(), anyString(), anyString(), any());

        publishedGame.editNode("testId", "testName",
                "testDesc", "testCondFlagId",
                GameNode.Condition.FlagState.ACTIVE, gameAuthorUser);
    }

    @Test(expected = Game.NodeNotFoundException.class)
    public void editNodeThrowsNodeNotFoundExceptionIfRootNodeThrowsNodeNotFoundException() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.NodeNotFoundException(""))
                .when(publishedGame.getRootNode()).editNode(anyString(),
                anyString(), anyString(), anyString(), any());

        publishedGame.editNode("testId", "testName",
                "testDesc", "testCondFlagId",
                GameNode.Condition.FlagState.ACTIVE, gameAuthorUser);
    }

    @Test(expected = NullPointerException.class)
    public void deleteNodeThrowsNullPointerExceptionIfNodeIdIsNull() throws Exception {
        publishedGame.deleteNode(null, new User());
    }

    @Test(expected = NullPointerException.class)
    public void deleteNodeThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        publishedGame.deleteNode("testId", null);
    }

    @Test(expected = Game.ForbiddenManipulationException.class)
    public void deleteNodeThrowsForbiddenManipulationExceptionIfUserCanNotModifyThatGame() throws Exception {
        publishedGame.deleteNode("testId", notGameAuthorUser);
    }

    @Test(expected = Game.RootNodeNotExistsException.class)
    public void deleteNodeThrowsRootNodeNotExistsExceptionIfRootNodeHasNotBeenAdded() throws Exception {
        publishedGame.deleteNode("testId", gameAuthorUser);
    }

    @Test
    public void deleteNodeDeletesNode() throws Exception {
        createMockRootNode(publishedGame);

        publishedGame.deleteNode("testId", gameAuthorUser);

        verify(publishedGame.getRootNode(), times(1))
                .deleteChildNode("testId");
    }

    @Test(expected = Game.NodeNotFoundException.class)
    public void deleteNodeThrowsNodeNotFoundExceptionIfRootNodeThrowsNodeNotFoundException() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.NodeNotFoundException(""))
                .when(publishedGame.getRootNode()).deleteChildNode(anyString());

        publishedGame.deleteNode("testId", gameAuthorUser);
    }

    @Test(expected = Game.ForbiddenManipulationException.class)
    public void deleteNodeThrowsForbiddenManipulationExceptionIfRootNodeThrowsRootNodeDeletingException() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.RootNodeDeletingException(""))
                .when(publishedGame.getRootNode()).deleteChildNode(anyString());

        publishedGame.deleteNode("testId", gameAuthorUser);
    }

    @Test(expected = Game.NodeVerificationException.class)
    public void deleteNodeThrowsNodeVerificationExceptionIfRootNodeThrowsNullValueException() throws Exception {
        createMockRootNode(publishedGame);

        doThrow(new GameNode.NullValueException(""))
                .when(publishedGame.getRootNode()).deleteChildNode(anyString());

        publishedGame.deleteNode("testId", gameAuthorUser);
    }

    @Test
    public void canBeViewedFromReturnsTrueFromNullUserIfGameIsPublished() {
        assertTrue(publishedGame.canBeViewedFrom(null));
    }

    @Test
    public void canBeViewedFromReturnsTrueFromUserWhichIsNotAuthorOfTheGameIfGameIsPublished() {
        assertTrue(publishedGame.canBeViewedFrom(notGameAuthorUser));
    }

    @Test
    public void canBeViewedFromReturnsTrueFromAuthorOfTheGameIfGameIsPublished() {
        assertTrue(publishedGame.canBeViewedFrom(gameAuthorUser));
    }

    @Test
    public void canBeViewedFromReturnsTrueFromAdminIfGameIsPublished() {
        assertTrue(publishedGame.canBeViewedFrom(adminUser));
    }

    @Test
    public void canBeViewedFromReturnsTrueFromAdminIfGameIsNotPublished() {
        assertTrue(unpublishedGame.canBeViewedFrom(adminUser));
    }

    @Test
    public void canBeViewedFromReturnsFalseFromUserWhichIsNotAuthorOfTheGameAndNotAdminIfGameIsNotPublished() {
        assertFalse(unpublishedGame.canBeViewedFrom(notGameAuthorUser));
    }

    @Test
    public void canBeViewedFromReturnsFalseFromNullUserIfGameIsNotPublished() {
        assertFalse(unpublishedGame.canBeViewedFrom(null));
    }

    @Test(expected = NullPointerException.class)
    public void canBeModifiedFromThrowsNullPointerExceptionIfUserIsNull() {
        publishedGame.canBeModifiedFrom(null);
    }

    @Test
    public void canBeModifiedFromReturnsTrueFromAuthorOfTheGame() {
        assertTrue(unpublishedGame.canBeModifiedFrom(gameAuthorUser));
    }

    @Test
    public void canBeModifiedFromReturnsTrueFromAdmin() {
        assertTrue(unpublishedGame.canBeModifiedFrom(adminUser));
    }

    @Test
    public void canBeModifiedFromReturnsFalseFromUserWhichIsNotAuthorOfTheGame() {
        assertFalse(unpublishedGame.canBeModifiedFrom(notGameAuthorUser));
    }

    @Test(expected = NullPointerException.class)
    public void Game_JsonReadyGame_ConstructorThrowsNullPointerExceptionIfGameIsNull() {
        new Game.JsonReadyGame(null);
    }

    @Test
    public void equalsShouldWorkProperly() throws Exception {
        val game1 = new Game();
        ReflectionTestUtils.setField(game1, "id", 1);
        val game2 = new Game();
        ReflectionTestUtils.setField(game2, "id", 2);

        val gameNode1 = new RoomNode("testRoomNode1", "testRoomName1",
                "testRoomDesc1");
        ReflectionTestUtils.setField(gameNode1, "id", "1");
        val gameNode2 = new RoomNode("testRoomNode2", "testRoomName2",
                "testRoomDesc2");
        ReflectionTestUtils.setField(gameNode2, "id", "2");

        val user1 = new User();
        ReflectionTestUtils.setField(user1, "id", 1);
        val user2 = new User();
        ReflectionTestUtils.setField(user2, "id", 2);

        EqualsVerifier.forClass(Game.class)
                .usingGetClass()
                .withPrefabValues(Game.class, game1, game2)
                .withPrefabValues(GameNode.class, gameNode1, gameNode2)
                .withPrefabValues(User.class, user1, user2)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }

    private User createSimpleUser(long id) throws Exception {
        val user = new User("username", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private User createAdminUser() throws Exception {
        val user = new User("username", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_ADMIN"));

        ReflectionTestUtils.setField(user, "id", 3L);
        return user;
    }

    private Game createPublishedGame() throws Exception {
        val game = new Game("name", "description",
                "language", gameAuthorUser, true);

        ReflectionTestUtils.setField(game, "id", 1L);
        ReflectionTestUtils.setField(game, "rating", 5.0);
        return game;
    }

    private Game createUnpublishedGame() throws Exception {
        val game = new Game("name", "description",
                "language", gameAuthorUser, false);

        ReflectionTestUtils.setField(game, "id", 1L);
        ReflectionTestUtils.setField(game, "rating", 5.0);
        return game;
    }

    private void createTestRootNode(Game game) throws Exception {
        val rootNode = new RoomNode("testId", "testName",
                "testDesc");

        ReflectionTestUtils.setField(rootNode, "id", "test");
        ReflectionTestUtils.setField(game, "rootNode", rootNode);
    }

    private void createMockRootNode(Game game) {
        val rootNode = mock(RoomNode.class);

        ReflectionTestUtils.setField(game, "rootNode", rootNode);
    }
}
