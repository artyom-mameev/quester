package com.artyommameev.quester.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Objects;

import static com.artyommameev.quester.QuesterApplication.MAX_LONG_STRING_SIZE;
import static com.artyommameev.quester.QuesterApplication.MIN_STRING_SIZE;

/**
 * A Comment domain entity. Encapsulates a comment that can be added to
 * a {@link Game}.
 *
 * @author Artyom Mameev
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    private long id;

    @Column(nullable = false)
    @Size(min = MIN_STRING_SIZE, max = MAX_LONG_STRING_SIZE)
    @Getter
    private String text;

    @Column(nullable = false)
    @Getter
    private Date date;

    @Getter
    private boolean edited;

    @JoinColumn(updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonBackReference
    @Getter
    private Game game;

    @JoinColumn(updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @Getter
    private User user;

    /**
     * Instantiates a new Comment with the current date.
     * <p>
     * After the object is created, it must be saved in the database
     * to be assigned a unique id.
     *
     * @param game a {@link Game} to which the comment should be added.
     * @param user an author {@link User} of the comment.
     * @param text the comment text.
     * @throws EmptyTextException if the comment text is empty.
     * @throws NullValueException if the comment text is null.
     */
    public Comment(@NonNull Game game, @NonNull User user, String text)
            throws EmptyTextException, NullValueException {
        this.game = game;
        this.user = user;
        this.date = new Date();
        edited = false;
        setText(text);
    }

    /**
     * Edits the text of the comment.
     * <p>
     * Changes the date of the comment to the current date and marks
     * the comment as edited.
     *
     * @param text a new comment text.
     * @param user a {@link User} who is trying to edit the comment text.
     * @throws ForbiddenManipulationException if the {@link User} is not allowed
     *                                        to edit the comment.
     * @throws EmptyTextException             if the new comment text is empty.
     * @throws NullValueException             if the new comment text is null.
     */
    public void editText(String text, @NonNull User user)
            throws ForbiddenManipulationException, EmptyTextException,
            NullValueException {
        if (!canBeModifiedBy(user)) {
            throw new ForbiddenManipulationException("Comment with id " +
                    id + " cannot be modified from user " + user.getUsername());
        }

        edited = true;
        date = new Date();
        setText(text);
    }

    /**
     * Checks whether an {@link User} can modify the comment.
     * <p>
     * {@link User} can modify the comment if they are the author of the comment
     * or if they are the admin.
     *
     * @param user a {@link User} to be checked if they can modify the comment.
     * @return true if the {@link User} is allowed to modify the comment,
     * otherwise false.
     */
    public boolean canBeModifiedBy(@NonNull User user) {
        return this.user.equals(user) || user.isAdmin();
    }

    /**
     * Checks if a two Comment objects are equal to each other.
     * <p>
     * Objects are equal if:
     * <p>
     * - None of the objects are null and their types are the same;<br>
     * - Their database ids are equal to each other.
     *
     * @param o the object to be checked.
     * @return a true boolean if the objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Comment comment = (Comment) o;

        return id == comment.id;
    }

    /**
     * Generates a hash code for the object based on the database id.
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
     * "Comment 'text' for game 'game' from user 'user'"
     */
    @Override
    public String toString() {
        return "Comment '" + text + "' for game '" +
                game.toString() + "' from user '" + user.toString() + "'";
    }

    private void setText(String text) throws NullValueException,
            EmptyTextException {
        if (text == null) {
            throw new NullValueException("Comment text cannot be null");
        }
        if (text.trim().isEmpty()) {
            throw new EmptyTextException("Comment text cannot be empty");
        }

        this.text = text.trim();
    }

    /**
     * An exception indicating that some sort of forbidden manipulation
     * attempt has been made to a {@link Comment}.
     */
    public static class ForbiddenManipulationException extends Exception {
        /**
         * Instantiates a new Forbidden Manipulation Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public ForbiddenManipulationException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a {@link Comment} text is empty.
     */
    public static class EmptyTextException extends Exception {
        /**
         * Instantiates a new Empty Text Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public EmptyTextException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that a {@link Comment} argument is null.
     */
    public static class NullValueException extends Exception {
        /**
         * Instantiates a new Null Value Exception.
         *
         * @param s the message that indicates the cause of exception.
         */
        public NullValueException(String s) {
            super(s);
        }
    }
}
