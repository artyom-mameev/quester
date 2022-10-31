package com.artyommameev.quester.repository;

import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * A repository that queries and stores {@link Comment} objects in the database.
 *
 * @author Artyom Mameev
 */
public interface CommentRepository
        extends PagingAndSortingRepository<Comment, Long> {

    /**
     * Queries {@link Comment}s for published {@link Game}s created by
     * a specific {@link User}, sorts them by date in descending order and
     * returns a specific {@link Page} depending on the pagination information.
     *
     * @param userId   an id of the {@link User} which created {@link Comment}s
     *                 that should be queried.
     * @param pageable the pagination information.
     * @return a {@link Page} with the {@link Comment}s selected and sorted
     * according to the criteria.
     */
    Page<Comment> findCommentsByUser_IdAndGame_PublishedIsTrueOrderByDateDesc(
            long userId, Pageable pageable);
}
