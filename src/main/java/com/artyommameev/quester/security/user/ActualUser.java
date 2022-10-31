package com.artyommameev.quester.security.user;

import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.service.UserService;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * An abstraction for unified processing of the current {@link User}, whether
 * is a normal or an oAuth2 user.
 *
 * @author Artyom Mameev
 */
@Component
@NoArgsConstructor
public class ActualUser {

    private UserService userService;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param userService a service that allows to query, interact and save
     *                    {@link User} objects.
     * @see UserService
     */
    @Autowired
    public ActualUser(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns the current {@link User}'s name.
     *
     * @return the current {@link User}'s name string;
     */
    public String getUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * Returns the current {@link User}'s password.
     *
     * @return the current {@link User}'s password string.
     */
    public String getPassword() {
        return getCurrentUser().getPassword();
    }

    /**
     * Returns the current {@link User}'s email.
     *
     * @return the current {@link User}'s email string.
     */
    public String getEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * Returns the current {@link User}'s id.
     *
     * @return the current {@link User}'s id.
     */
    public long getId() {
        return getCurrentUser().getId();
    }

    /**
     * Returns the current {@link User}'s google username.<br>
     * Should only be used if the current {@link User} is an oAuth2 user.
     *
     * @return the current {@link User}'s google username string.
     */
    public String getGoogleUsername() {
        if (!isOauth2User()) {
            throw new RuntimeException(getCurrentUser() + " is not" +
                    " a Google user");
        }

        return getCurrentUser().getGoogleUsername();
    }

    /**
     * Returns the current {@link User}'s google email.<br>
     * Should only be used if the current {@link User} is an oAuth2 user.
     *
     * @return the current {@link User}'s google email string.
     */
    public String getGoogleEmail() {
        if (!isOauth2User()) {
            throw new RuntimeException(getCurrentUser() + " is not" +
                    "a Google user.");
        }

        return getCurrentUser().getGoogleEmail();
    }

    /**
     * Checks if the current {@link User} has an unconfirmed username.
     *
     * @return true if the username is unconfirmed, otherwise false.
     */
    public boolean hasUnconfirmedUsername() {
        if (!isOauth2User()) {
            return false;
        }

        val oauth2User = getCurrentUser();

        return oauth2User.getUsername() == null;

    }

    /**
     * Returns current logged normal or oAuth2 {@link User}.
     *
     * @return the current logged normal or oAuth2 {@link User} or
     * null if no normal or oAuth2 {@link User} is authenticated.
     * @throws DisabledException if the {@link User} is disabled.
     */
    public User getCurrentUser() {
        if (!isLoggedIn()) {
            return null;
        }

        User user = null;

        if (isOauth2User()) {
            user = getOauth2User();
        } else if (isNormalUser()) {
            user = getNormalUser();
        }

        if (!Objects.requireNonNull(user).isEnabled())
            throw new DisabledException("User " + user + " is disabled");

        return user;

    }

    /**
     * Checks if the current {@link User} is an oAuth user.
     * Should only be used if the current {@link User} is logged in.
     *
     * @return true if the current {@link User} is an oAuth user, otherwise
     * false.
     */
    public boolean isOauth2User() {
        if (!isLoggedIn()) {
            throw new RuntimeException("User is not logged in");
        }

        return getPrincipal() instanceof OAuth2User;
    }

    /**
     * Checks if the current {@link User} is a normal user.
     * Should only be used if the current {@link User} is logged in.
     *
     * @return true if the current {@link User} is a normal user, otherwise
     * false.
     */
    public boolean isNormalUser() {
        if (!isLoggedIn()) {
            throw new RuntimeException("User is not logged in");
        }

        return getPrincipal() instanceof User;
    }

    /**
     * Checks if the current {@link User} is logged in.
     *
     * @return true if the current {@link User} is logged in, otherwise false.
     */
    public boolean isLoggedIn() {
        val principal = getPrincipal();

        return principal instanceof User || principal instanceof OAuth2User;
    }

    private Object getPrincipal() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
    }

    private User getNormalUser() {
        val userPrincipal = (User) getPrincipal();

        try {
            return userService.getUser(userPrincipal.getId());
        } catch (UserService.UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private User getOauth2User() {
        val oidcUser = (OAuth2User) getPrincipal();

        try {
            return userService.getGoogleUser(Objects.requireNonNull(
                    oidcUser.getAttribute("email")));
        } catch (UserService.UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}