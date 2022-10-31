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
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("ConstantConditions")
public class CommentTests {

    @Mock
    private User user;
    @Mock
    private User user2;
    @Mock
    private User admin;
    @Mock
    private Game game;

    @Before
    public void setUp() {
        setUpUserMock(user, false);
        setUpUserMock(user2, false);
        setUpUserMock(admin, true);
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsNullPointerExceptionIfGameIsNull() throws Exception {
        new Comment(null, user, "text");
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        new Comment(game, null, "text");
    }

    @Test(expected = Comment.NullValueException.class)
    public void constructorThrowsNullPointerExceptionIfTextIsNull() throws Exception {
        new Comment(game, user, null);
    }

    @Test(expected = Comment.EmptyTextException.class)
    public void constructorThrowsEmptyTextExceptionIfTrimmedTextIsEmpty() throws Exception {
        new Comment(game, user, " ");
    }

    @Test
    public void constructorSetsTrimmedText() throws Exception {
        val comment = new Comment(game, user, " text ");

        assertEquals("text", comment.getText());
    }

    @Test
    public void editTextEditsTextWithTrimmedTextFromSameUser() throws Exception {
        val comment = new Comment(game, user, "testText");

        comment.editText(" testText2 ", user);

        assertEquals("testText2", comment.getText());
    }

    @Test
    public void editTextEditsTextWithTrimmedTextFromAdmin() throws Exception {
        val comment = new Comment(game, user, "testText");

        comment.editText(" testText2 ", admin);

        assertEquals("testText2", comment.getText());
    }

    @Test
    public void editTextSetsEditedFromSameUser() throws Exception {
        val comment = new Comment(game, user, "testText");

        comment.editText(" testText2 ", user);

        assertTrue(comment.isEdited());
    }

    @Test
    public void editTextSetsEditedFromAdmin() throws Exception {
        val comment = new Comment(game, user, "testText");

        assertFalse(comment.isEdited());

        comment.editText(" testText2 ", admin);

        assertTrue(comment.isEdited());
    }

    @Test
    public void editTextSetsNewDateFromSameUser() throws Exception {
        val comment = new Comment(game, user, "testText");

        val oldDate = comment.getDate();

        Thread.sleep(1); // to change the current time

        comment.editText(" testText2 ", user);

        assertNotEquals(oldDate, comment.getDate());
    }

    @Test
    public void editTextSetsNewDateFromAdmin() throws Exception {
        val comment = new Comment(game, user, "testText");

        val oldDate = comment.getDate();

        Thread.sleep(1); // to change the current time

        comment.editText(" testText2 ", admin);

        assertNotEquals(oldDate, comment.getDate());
    }

    @Test(expected = Comment.NullValueException.class)
    public void editTextThrowsNullValueExceptionIfTextIsNull() throws Exception {
        val comment = new Comment(game, user, "testText");

        comment.editText(null, user);
    }

    @Test(expected = NullPointerException.class)
    public void editTextThrowsNullValueExceptionIfUserIsNull() throws Exception {
        val comment = new Comment(game, user, "testText");

        comment.editText("text", null);
    }

    @Test(expected = Comment.EmptyTextException.class)
    public void editTextThrowsEmptyTextExceptionIfTrimmedTextIsEmpty() throws Exception {
        val comment = new Comment(game, user, "testText");

        comment.editText(" ", user);
    }

    @Test(expected = Comment.ForbiddenManipulationException.class)
    public void editTextThrowsForbiddenManipulationExceptionFromOtherUser() throws Exception {
        val comment = new Comment(game, user, "testText");

        comment.editText("testText2", user2);
    }

    @Test(expected = NullPointerException.class)
    public void canBeModifiedByThrowsNullValueExceptionIfUserIsNull() throws Exception {
        val comment = new Comment(game, user, "testText");

        comment.canBeModifiedBy(null);
    }

    @Test
    public void canBeModifiedByIsTrueIfUserIsSame() throws Exception {
        val comment = new Comment(game, user, "testText");

        assertTrue(comment.canBeModifiedBy(user));
    }

    @Test
    public void canBeModifiedByIsFalseIfUserIsNotSame() throws Exception {
        val comment = new Comment(game, user, "testText");

        assertFalse(comment.canBeModifiedBy(user2));
    }

    @Test
    public void canBeModifiedByIsTrueIfUserIsAdmin() throws Exception {
        val comment = new Comment(game, user, "testText");

        assertTrue(comment.canBeModifiedBy(admin));
    }

    @Test
    public void equalsWorksProperly() throws Exception {
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

        val review1 = new Review();
        ReflectionTestUtils.setField(review1, "id", 1);
        val review2 = new Review();
        ReflectionTestUtils.setField(review2, "id", 2);

        val user1 = new User();
        ReflectionTestUtils.setField(user1, "id", 1);
        val user2 = new User();
        ReflectionTestUtils.setField(user2, "id", 2);

        EqualsVerifier.forClass(Comment.class)
                .usingGetClass()
                .withPrefabValues(Game.class, game1, game2)
                .withPrefabValues(GameNode.class, gameNode1, gameNode2)
                .withPrefabValues(Review.class, review1, review2)
                .withPrefabValues(User.class, user1, user2)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }

    private void setUpUserMock(User user, boolean isAdmin) {
        when(user.isAdmin()).thenReturn(isAdmin);
    }
}
