package com.artyommameev.quester.service;

import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.Review;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.repository.UserRepository;
import lombok.NonNull;
import lombok.val;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * A service that allows to query, interact, and save {@link User} objects.
 * <p>
 * All exceptions related to verification of the {@link User} objects
 * constraints are handled as a {@link VerificationException} in order to
 * keep abstraction levels apart and with an eye toward the API design where's
 * no need to handle verification exceptions differently - the service is
 * designed to handle client data whose errors do not make sense to handle
 * differently - it is enough to return an error to the client, so the service
 * wraps verification errors in an exception corresponding to the current
 * abstraction level, and the true cause of the exception (in order to notify
 * the client about it) can be retrieved from the root exception.
 *
 * @author Artyom Mameev
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param userRepository  a repository for querying and saving {@link User}
     *                        objects.
     * @param passwordEncoder a password encoder.
     */
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new {@link User} account.
     *
     * @param username         a name of the new {@link User}.
     * @param password         a password of the new {@link User}.
     * @param matchingPassword the matching password.
     * @param email            an email of the new {@link User}.
     * @throws UserAlreadyExistsException if {@link User} with the given email
     *                                    or username already exists.
     * @throws VerificationException      if an error occurs during verification
     *                                    of the {@link User} data.
     */
    public void registerNewUserAccount(String username, String password,
                                       String matchingPassword, String email)
            throws UserAlreadyExistsException, VerificationException {
        if (usernameExists(username.trim())) {
            throw new UserAlreadyExistsException("User with username " +
                    username + " already exists!");
        }
        if (emailExists((email.trim()))) {
            throw new UserAlreadyExistsException("User with E-mail " +
                    email + " already exists!");
        }

        try {
            val user = new User(username, email, password, matchingPassword,
                    passwordEncoder, Collections.singletonList("ROLE_USER"));

            userRepository.save(user);
        } catch (User.EmptyStringException | User.EmailFormatException |
                User.PasswordsNotMatchException |
                User.NullValueException e) {
            throw new VerificationException(e);
        }

    }

    /**
     * Updates a {@link User} credentials.
     * <p>
     * Passwords can be empty. In this case, the {@link User}'s password
     * will not be changed.
     *
     * @param user             the {@link User} which credentials should be
     *                         changed.
     * @param email            the new {@link User} email.
     * @param password         the new {@link User} password.
     * @param matchingPassword the matching password.
     * @throws VerificationException      if an error occurs during verification
     *                                    of the new {@link User} credentials.
     * @throws UserAlreadyExistsException if {@link User} with the given email
     *                                    already exists.
     */
    public void updateUserAccount(@NonNull User user, String email,
                                  String password, String matchingPassword)
            throws VerificationException, UserAlreadyExistsException {
        if (emailExists(email)) {
            throw new UserAlreadyExistsException("User with E-mail " +
                    email + " already exists!");
        }

        try {
            user.updateEmail(email);
        } catch (User.EmailFormatException | User.NullValueException e) {
            throw new VerificationException(e);
        }

        try {
            user.updatePassword(password, matchingPassword, passwordEncoder);
        } catch (User.EmptyStringException e) {
            // if the passwords are empty, they just don't changes
        } catch (User.PasswordsNotMatchException | User.NullValueException e) {
            throw new VerificationException(e);
        }

        userRepository.save(user);
    }

    /**
     * Returns a {@link User} by id.
     *
     * @param id the id of the {@link User} that should be returned.
     * @return a {@link User} with the given id.
     * @throws UserNotFoundException if the {@link User} is not found.
     */
    public User getUser(long id) throws UserNotFoundException {
        val optionalUser = userRepository.findById(id);

        return optionalUser.orElseThrow(() -> new UserNotFoundException(
                "The user with the id: " + id + " is not found"));
    }

    /**
     * Returns a google {@link User} by google email.
     *
     * @param googleEmail the google email of the {@link User} that should
     *                    be returned.
     * @return a google {@link User} with the given google email.
     * @throws UserNotFoundException if the {@link User} is not found.
     */
    public User getGoogleUser(@NonNull String googleEmail)
            throws UserNotFoundException {
        val optionalUser = userRepository.findUserByGoogleEmail(
                googleEmail);

        return optionalUser.orElseThrow(() -> new UserNotFoundException(
                "The user with the google email: " + googleEmail +
                        " is not found"));
    }

    /**
     * Checks if a {@link User} with a specific username exists in the database.
     *
     * @param username the username to check.
     * @return true if the {@link User} exists in the database, otherwise false.
     */
    public boolean usernameExists(@NonNull String username) {
        return userRepository.findUserByUsername(username).isPresent();
    }

    /**
     * Checks if a {@link User} with a specific email exists in the database.
     *
     * @param email the email to check.
     * @return true if the {@link User} exists in the database, otherwise false.
     */
    public boolean emailExists(@NonNull String email) {
        return userRepository.findUserByEmail(email).isPresent();
    }

    /**
     * Confirms username of an oAuth2 {@link User}.
     *
     * @param oauth2User the oAuth2 {@link User} whose username should be
     *                   confirmed.
     * @param username   the username of the oAuth2 {@link User} to confirm.
     * @throws VerificationException     if an error occurs during verification
     *                                   of the username.
     * @throws AlreadyConfirmedException if the name of the user is already
     *                                   confirmed.
     */
    public void confirmUsernameOfOauth2User(@NonNull User oauth2User,
                                            String username)
            throws VerificationException, AlreadyConfirmedException {
        try {
            oauth2User.confirmUsername(username);

            userRepository.save(oauth2User);
        } catch (User.EmptyStringException | User.NullValueException e) {
            throw new VerificationException(e);
        } catch (User.UsernameExistsException e) {
            throw new AlreadyConfirmedException(e);
        }
    }

    /**
     * Changes enabled status of a {@link User}.
     *
     * @param userId      an id of the {@link User} which enabled status
     *                    should be changed.
     * @param enabled     the new enabled status of the {@link User}.
     * @param currentUser a {@link User} that tries to change enabled
     *                    status of the {@link User}.
     * @throws UserNotFoundException if the {@link User} is not found.
     * @throws ForbiddenException    if the given {@link User} is not allowed
     *                               to change the enabled status of the
     *                               {@link User}
     */
    public void setEnabled(long userId, boolean enabled,
                           @NonNull User currentUser)
            throws UserNotFoundException, ForbiddenException {
        val user = getUser(userId);

        try {
            user.setEnabled(enabled, currentUser);

            userRepository.save(user);
        } catch (User.ForbiddenManipulationException e) {
            throw new ForbiddenException(e);
        }
    }

    /**
     * Removes all {@link Game}s created by a {@link User}.
     *
     * @param userId      an id of the {@link User} whose {@link Game}s
     *                    should be removed.
     * @param currentUser a {@link User} that tries to remove all
     *                    {@link Game}s created by the {@link User}.
     * @throws UserNotFoundException if the {@link User} is not found.
     * @throws ForbiddenException    if the given {@link User} is not allowed
     *                               to remove {@link Game}s created by the
     *                               {@link User}.
     */
    public void deleteUserGames(long userId, @NonNull User currentUser)
            throws UserNotFoundException, ForbiddenException {
        val user = getUser(userId);

        try {
            user.clearGames(currentUser);

            userRepository.save(user);
        } catch (User.ForbiddenManipulationException e) {
            throw new ForbiddenException(e);
        }
    }

    /**
     * Removes all {@link Review}s created by a {@link User}.
     *
     * @param userId      an id of the {@link User} whose {@link Review}s
     *                    should be removed.
     * @param currentUser a {@link User} that tries to remove all
     *                    {@link Review}s created by the {@link User}.
     * @throws UserNotFoundException if the {@link User} is not found.
     * @throws ForbiddenException    if the given {@link User} is not allowed
     *                               to remove the {@link Review}s created by
     *                               the {@link User}.
     */
    public void deleteUserReviews(long userId, @NonNull User currentUser)
            throws UserNotFoundException, ForbiddenException {
        val user = getUser(userId);

        try {
            user.clearReviews(currentUser);

            userRepository.save(user);
        } catch (User.ForbiddenManipulationException e) {
            throw new ForbiddenException(e);
        }
    }

    /**
     * Removes all {@link Comment}s created by a {@link User}.
     *
     * @param userId      an id of the {@link User} whose {@link Comment}s
     *                    should be removed.
     * @param currentUser a {@link User} that tries to remove all
     *                    {@link Comment}s created by the {@link User}.
     * @throws UserNotFoundException if the {@link User} is not found.
     * @throws ForbiddenException    if the given {@link User} is not allowed
     *                               to remove the {@link Comment}s created
     *                               by the {@link User}.
     */
    public void deleteUserComments(long userId, @NonNull User currentUser)
            throws UserNotFoundException, ForbiddenException {
        val user = getUser(userId);

        try {
            user.clearComments(currentUser);

            userRepository.save(user);
        } catch (User.ForbiddenManipulationException e) {
            throw new ForbiddenException(e);
        }
    }

    /**
     * An exception indicating that {@link User} with specific credentials
     * already exists.
     */
    public static class UserAlreadyExistsException extends Exception {
        /**
         * Instantiates a new User Already Exists Exception.
         *
         * @param s the message that indicates the cause of the exception.
         */
        public UserAlreadyExistsException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that an oAuth2 {@link User} already confirmed
     * their username.
     */
    public static class AlreadyConfirmedException extends Exception {
        /**
         * Instantiates a new Already Confirmed Exception.
         *
         * @param t the cause of the exception.
         */
        public AlreadyConfirmedException(Throwable t) {
            super(t);
        }
    }

    /**
     * An exception indicating that a {@link User} is not found.
     */
    public static class UserNotFoundException extends Exception {
        /**
         * Instantiates a new User Not Found Exception.
         *
         * @param s the message that indicates the cause of the exception.
         */
        public UserNotFoundException(String s) {
            super(s);
        }
    }

    /**
     * An exception indicating that some verification errors has been
     * occurred while processing a {@link User} entity.
     */
    public static class VerificationException extends Exception {
        /**
         * Instantiates a new Verification Exception.
         *
         * @param t the cause of the exception.
         */
        public VerificationException(Throwable t) {
            super(t);
        }
    }

    /**
     * An exception indicating that some sort of forbidden manipulation
     * attempt has been made to a {@link User}.
     */
    public static class ForbiddenException extends Exception {
        /**
         * Instantiates a new Forbidden Exception.
         *
         * @param s the message that indicates the cause of the exception.
         */
        public ForbiddenException(String s) {
            super(s);
        }

        /**
         * Instantiates a new Forbidden Exception.
         *
         * @param t the cause of the exception.
         */
        public ForbiddenException(Throwable t) {
            super(t);
        }
    }
}