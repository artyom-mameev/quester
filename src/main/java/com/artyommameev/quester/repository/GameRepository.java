package com.artyommameev.quester.repository;

import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * A repository that queries and stores {@link Game} objects in the database.
 *
 * @author Artyom Mameev
 */
public interface GameRepository extends PagingAndSortingRepository<Game, Long> {

    /**
     * Queries all published {@link Game}s, sorts them by date and returns
     * a specific {@link Page} depending on the pagination information.
     *
     * @param pageable the pagination information.
     * @return a {@link Page} with {@link Game}s selected and sorted
     * according to the criteria.
     */
    Page<Game> findGamesByPublishedIsTrueOrderByDate(Pageable pageable);

    /**
     * Queries all published {@link Game}s, sorts them by date in descending
     * order and returns a specific {@link Page} depending on the pagination
     * information.
     *
     * @param pageable the pagination information.
     * @return a {@link Page} with {@link Game}s selected and sorted according
     * to the criteria.
     */
    Page<Game> findGamesByPublishedIsTrueOrderByDateDesc(Pageable pageable);

    /**
     * Queries all published {@link Game}s, sorts them by rating and returns
     * a specific {@link Page} depending on the pagination information.
     *
     * @param pageable the pagination information.
     * @return a {@link Page} with {@link Game}s selected and sorted according
     * to the criteria.
     */
    Page<Game> findGamesByPublishedIsTrueOrderByRatingDesc(Pageable pageable);

    /**
     * Queries published {@link Game}s created by a specific {@link User}, sorts
     * them by date and returns a specific {@link Page} depending on the
     * pagination information.
     *
     * @param userId   an id of the {@link User} which created {@link Game}s
     *                 that should be queried.
     * @param pageable the pagination information.
     * @return a {@link Page} with {@link Game}s selected and sorted according
     * to the criteria.
     */
    Page<Game> findGamesByUser_IdAndPublishedIsTrueOrderByDate(
            long userId, Pageable pageable);

    /**
     * Queries published {@link Game}s created by a specific {@link User}, sorts
     * them by date in descending order and returns a specific {@link Page}
     * depending on the pagination information.
     *
     * @param userId   an id of the {@link User} which created {@link Game}s
     *                 that should be queried.
     * @param pageable the pagination information.
     * @return a {@link Page} with {@link Game}s selected and sorted according
     * to the criteria.
     */
    Page<Game> findGamesByUser_IdAndPublishedIsTrueOrderByDateDesc(
            long userId, Pageable pageable);

    /**
     * Queries published {@link Game}s created by a specific {@link User}, sorts
     * them by rating and returns a specific {@link Page} depending on the
     * pagination information.
     *
     * @param userId   an id of the {@link User} which {@link Game}s should be
     *                 queried.
     * @param pageable the pagination information.
     * @return a {@link Page} with {@link Game}s selected and sorted according
     * to the criteria.
     */
    Page<Game> findGamesByUser_IdAndPublishedIsTrueOrderByRatingDesc(
            long userId, Pageable pageable);

    /**
     * Queries published {@link Game}s which have been added to the favorites
     * of a specific {@link User}, sorts them by date and returns a specific
     * {@link Page} depending on the pagination information.
     *
     * @param user     the {@link User} which favorited {@link Game}s should be
     *                 queried.
     * @param pageable the pagination information.
     * @return a {@link Page} with {@link Game}s selected and sorted according
     * to the criteria.
     */
    Page<Game> findAllByFavoritedIsContainingAndPublishedIsTrueOrderByDate(
            User user, Pageable pageable);

    /**
     * Queries published {@link Game}s which have been added to the favorites
     * of a specific {@link User}, sorts them by date in descending order
     * and returns a specific {@link Page} depending on the pagination
     * information.
     *
     * @param user     the {@link User} which favorited {@link Game}s should be
     *                 queried.
     * @param pageable the pagination information.
     * @return a {@link Page} with {@link Game}s selected and sorted according
     * to the criteria.
     */
    Page<Game> findAllByFavoritedIsContainingAndPublishedIsTrueOrderByDateDesc(
            User user, Pageable pageable);

    /**
     * Queries published {@link Game}s which have been added to the favorites
     * of a specific {@link User}, sorts them by rating and returns a specific
     * {@link Page} depending on the pagination information.
     *
     * @param user     the {@link User} which favorited {@link Game}s should be
     *                 queried.
     * @param pageable the pagination information.
     * @return a {@link Page} with {@link Game}s selected and sorted according
     * to the criteria.
     */
    Page<Game> findAllByFavoritedIsContainingAndPublishedIsTrueOrderByRatingDesc(
            User user, Pageable pageable);

    /**
     * Queries unpublished {@link Game}s created by a specific {@link User},
     * sorts them by date and returns a specific {@link Page} depending on the
     * pagination information.
     *
     * @param user     the {@link User} which unpublished {@link Game}s should
     *                 be queried.
     * @param pageable the pagination information.
     * @return a {@link Page} with {@link Game}s selected and sorted according
     * to the criteria.
     */
    Page<Game> findGamesByUserAndPublishedIsFalseOrderByDate(
            User user, Pageable pageable);
}