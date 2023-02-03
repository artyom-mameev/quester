package com.artyommameev.quester.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import java.util.Objects;

/**
 * A Review domain entity. Encapsulates a review that can be given to
 * a {@link Game}.
 *
 * @author Artyom Mameev
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private long id;

    @Range(min = 1, max = 5)
    @Getter
    private int rating;

    @JoinColumn(updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @Getter
    private Game game;

    @JoinColumn(updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @Getter
    private User user;

    /**
     * Instantiates a new Review.
     * <p>
     * After the object is created, it must be saved in the database
     * to be assigned a unique id.
     *
     * @param rating a {@link Rating} of the {@link Review}
     * @param game   a {@link Game} that reviewed.
     * @param user   a {@link User} that reviews the {@link Game}.
     * @see Rating
     */
    public Review(Review.Rating rating, @NonNull Game game,
                  @NonNull User user) {
        this.rating = rating.getRating();
        this.game = game;
        this.user = user;
    }

    /**
     * Changes the {@link Rating} of the review.
     *
     * @param rating the new {@link Rating} of the review.
     */
    public void updateRating(Review.Rating rating) {
        this.rating = rating.getRating();
    }

    /**
     * Checks if two Review objects are equal to each other.
     * <p>
     * Objects are equal if:
     * <p>
     * - None of the objects are null and their types are the same;<br>
     * - Their database ids is equal to each other.
     *
     * @param o the object to be checked.
     * @return true if objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Review review = (Review) o;

        return id == review.id;
    }

    /**
     * Generates hash code for the object based on the database id.
     *
     * @return the hash code for the object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns String representation of the object.
     *
     * @return the hash code for the object in the format:
     * "Review with rating 'rating' for game 'game' from user 'user'"
     */
    @Override
    public String toString() {
        return "Review with rating '" + rating + "' for game '" +
                game.toString() + "' from user '" + user.toString() + "'";
    }

    /**
     * The enumeration that represents a numerical rating from 1 to 5.
     */
    public enum Rating {
        ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);

        private final int rating;

        Rating(int rating) {
            this.rating = rating;
        }

        private static Rating fromInt(int rating) throws WrongRatingException {
            switch (rating) {
                case 1:
                    return ONE;
                case 2:
                    return TWO;
                case 3:
                    return THREE;
                case 4:
                    return FOUR;
                case 5:
                    return FIVE;
                default:
                    throw new WrongRatingException("Rating can be only" +
                            " an integer from 1 to 5");
            }
        }

        /**
         * Returns a {@link Rating} enumeration member which corresponds to
         * a certain numeric rating in string format.
         *
         * @param rating the numeric rating string that should be returned as
         *               a {@link Rating} enumeration member.
         * @return the enumeration member based on the numeric rating string.
         * @throws NullRatingException      if the numeric rating string is null.
         * @throws NotNumberRatingException if the numeric rating string is not
         *                                  a number.
         * @throws WrongRatingException     if the numeric rating string is not
         *                                  a number from '1' to '5'.
         */
        public static Rating fromString(String rating) throws NullRatingException,
                NotNumberRatingException, WrongRatingException {
            if (rating == null) {
                throw new NullRatingException("Rating cannot be null");
            }
            if (!NumberUtils.isCreatable(rating)) {
                throw new NotNumberRatingException("Rating can only be" +
                        " a number");
            }

            return fromInt(Integer.parseInt(rating));
        }

        /**
         * Returns rating value in numeric format.
         *
         * @return the rating value in numeric format.
         */
        public int getRating() {
            return rating;
        }
    }

    /**
     * An exception indicating that a certain {@link Rating} value is null.
     */
    public static class NullRatingException extends Exception {
        /**
         * Instantiates a new Null Rating Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public NullRatingException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a certain {@link Rating} value is incorrect.
     */
    public static class WrongRatingException extends Exception {
        /**
         * Instantiates a new Wrong Rating Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public WrongRatingException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a certain {@link Rating} value is not
     * a number.
     */
    public static class NotNumberRatingException extends Exception {
        /**
         * Instantiates a new Not Number Rating Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public NotNumberRatingException(String s) {
            super(s);
        }
    }
}