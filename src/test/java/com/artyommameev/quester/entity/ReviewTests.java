package com.artyommameev.quester.entity;

import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.entity.gamenode.RoomNode;
import lombok.val;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(JUnit4.class)
@SuppressWarnings("ConstantConditions")
public class ReviewTests {

    @Test(expected = NullPointerException.class)
    public void constructorThrowsNullPointerExceptionIfGameIsNull() {
        new Review(Review.Rating.FOUR, null, new User());
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsNullPointerExceptionIfUserIsNull() {
        new Review(Review.Rating.FOUR, new Game(), null);
    }

    @Test
    public void constructorSetsRating() {
        var review = new Review(Review.Rating.ONE, new Game(), new User());

        assertEquals(1, review.getRating());

        review = new Review(Review.Rating.TWO, new Game(), new User());

        assertEquals(2, review.getRating());

        review = new Review(Review.Rating.THREE, new Game(), new User());

        assertEquals(3, review.getRating());

        review = new Review(Review.Rating.FOUR, new Game(), new User());

        assertEquals(4, review.getRating());

        review = new Review(Review.Rating.FIVE, new Game(), new User());

        assertEquals(5, review.getRating());
    }

    @Test
    public void updateRatingUpdatesRating() {
        var review = new Review(Review.Rating.TWO, new Game(), new User());

        review.updateRating(Review.Rating.ONE);

        assertEquals(1, review.getRating());

        review.updateRating(Review.Rating.TWO);

        assertEquals(2, review.getRating());

        review.updateRating(Review.Rating.THREE);

        assertEquals(3, review.getRating());

        review.updateRating(Review.Rating.FOUR);

        assertEquals(4, review.getRating());

        review.updateRating(Review.Rating.FIVE);

        assertEquals(5, review.getRating());
    }

    @Test(expected = Review.NullRatingException.class)
    public void Review_Rating_fromStringThrowsNullRatingExceptionIfRatingIsNull() throws Exception {
        Review.Rating.fromString(null);
    }

    @Test(expected = Review.NotNumberRatingException.class)
    public void Review_Rating_fromStringThrowsNotNumberRatingExceptionIfRatingIsNotNumber() throws Exception {
        Review.Rating.fromString("notNumber");
    }

    @Test
    public void Review_Rating_fromStringThrowsWrongRatingExceptionIfRatingIsNotNumberFromOneToFive() {
        assertThrows(Review.WrongRatingException.class, () ->
                Review.Rating.fromString("0"));

        assertThrows(Review.WrongRatingException.class, () ->
                Review.Rating.fromString("6"));
    }

    @Test
    public void Review_Rating_fromStringCreatesRatingFromString() throws Exception {
        assertEquals(Review.Rating.ONE, Review.Rating.fromString("1"));
        assertEquals(Review.Rating.TWO, Review.Rating.fromString("2"));
        assertEquals(Review.Rating.THREE, Review.Rating.fromString("3"));
        assertEquals(Review.Rating.FOUR, Review.Rating.fromString("4"));
        assertEquals(Review.Rating.FIVE, Review.Rating.fromString("5"));
    }

    @Test(expected = Review.WrongRatingException.class)
    public void Review_Rating_fromStringThrowsWrongRatingExceptionIfRatingLessThanOne() throws Exception {
        Review.Rating.fromString("0");
    }

    @Test(expected = Review.WrongRatingException.class)
    public void Review_Rating_fromStringThrowsWrongRatingExceptionIfRatingMoreThanFive() throws Exception {
        Review.Rating.fromString("6");
    }

    @Test
    public void Review_Rating_getRatingReturnsRatingAsInteger() {
        assertEquals(1, Review.Rating.ONE.getRating());
        assertEquals(2, Review.Rating.TWO.getRating());
        assertEquals(3, Review.Rating.THREE.getRating());
        assertEquals(4, Review.Rating.FOUR.getRating());
        assertEquals(5, Review.Rating.FIVE.getRating());
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

        val review1 = new Review();
        ReflectionTestUtils.setField(review1, "id", 1);
        val review2 = new Review();
        ReflectionTestUtils.setField(review2, "id", 2);

        val user1 = new User();
        ReflectionTestUtils.setField(user1, "id", 1);
        val user2 = new User();
        ReflectionTestUtils.setField(user2, "id", 2);

        EqualsVerifier.forClass(Review.class)
                .usingGetClass()
                .withPrefabValues(Game.class, game1, game2)
                .withPrefabValues(GameNode.class, gameNode1, gameNode2)
                .withPrefabValues(Review.class, review1, review2)
                .withPrefabValues(User.class, user1, user2)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }
}
