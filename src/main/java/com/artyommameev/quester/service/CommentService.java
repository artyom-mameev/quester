package com.artyommameev.quester.service;

import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service that allows to query {@link Comment} objects.
 *
 * @author Artyom Mameev
 */
@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param commentRepository a repository for querying and saving
     *                          {@link Comment} objects.
     * @see CommentRepository
     */
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Returns a {@link Page} with {@link Comment}s from a specific {@link User}.
     *
     * @param userId an id of the {@link User} whose {@link Comment}s
     *               should be returned.
     * @param size   the {@link Page} size.
     * @param page   the {@link Page} number.
     * @return a {@link Page} with {@link Comment}s from a specific
     * {@link User} according to the given {@link Page} size and
     * {@link Page} number.
     * @throws IllegalPageValueException if the {@link Page} number < 0.
     */
    public Page<Comment> getUserCommentsPage(long userId, int size,
                                             int page)
            throws IllegalPageValueException {
        if (page < 0) {
            throw new IllegalPageValueException("The page value cannot be " +
                    "less than 0!");
        }

        return commentRepository
                .findCommentsByUser_IdAndGame_PublishedIsTrueOrderByDateDesc(
                        userId, PageRequest.of(page, size));
    }

    /**
     * An exception indicating that a specific {@link Page} value is incorrect.
     */
    public static class IllegalPageValueException extends Exception {
        /**
         * Instantiates a new Illegal Page Value Exception.
         *
         * @param s the cause of the exception.
         */
        public IllegalPageValueException(String s) {
            super(s);
        }
    }
}
