package com.artyommameev.quester.repository;

import com.artyommameev.quester.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * A repository that queries and stores {@link User} objects in the database.
 *
 * @author Artyom Mameev
 */
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Queries {@link User} by email.
     *
     * @param email the email of the {@link User} that should be queried.
     * @return an {@link Optional} of the {@link User}.
     */
    Optional<User> findUserByEmail(String email);

    /**
     * Queries {@link User} by username.
     *
     * @param username the name of the {@link User} that should be queried.
     * @return an {@link Optional} of the {@link User}.
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Queries {@link User} by google email.
     *
     * @param googleEmail the google email of the {@link User} that should be
     *                    queried.
     * @return an {@link Optional} of the {@link User}.
     */
    Optional<User> findUserByGoogleEmail(String googleEmail);
}
