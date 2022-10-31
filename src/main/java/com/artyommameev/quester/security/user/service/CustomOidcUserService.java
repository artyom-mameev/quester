package com.artyommameev.quester.security.user.service;

import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.repository.UserRepository;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * A custom {@link OidcUserService} that saves new {@link OidcUser} to
 * the database.
 *
 * @author Artyom Mameev
 */
@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final MessageSource messageSource;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param userRepository a repository that allows to query and store
     *                       the {@link User} objects.
     * @param messageSource  the {@link MessageSource} for internationalization
     *                       purposes.
     * @see UserRepository
     */
    public CustomOidcUserService(UserRepository userRepository,
                                 MessageSource messageSource) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    /**
     * Loads and processes {@link OidcUser}.
     * <p>
     * The user's email is searched in the database to see if a {@link User}
     * with that google email is already saved. If there is no such {@link User},
     * it is created and saved.
     *
     * @param userRequest the {@link OidcUserRequest}.
     * @throws OAuth2AuthenticationException if the found {@link User} is
     *                                       disabled.
     */
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {
        val oidcUser = super.loadUser(userRequest);

        try {
            return processOidcUser(oidcUser);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(),
                    ex.getCause());
        }
    }

    private OidcUser processOidcUser(OidcUser oidcUser) {
        val optionalUser = userRepository.findUserByGoogleEmail(
                Objects.requireNonNull(oidcUser.getAttribute("email")));

        if (optionalUser.isEmpty()) {
            val newGoogleUser = User.fromGoogleOidcUser(oidcUser);
            userRepository.save(newGoogleUser);
        } else if (!optionalUser.get().isEnabled()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    "invalid_token", messageSource.getMessage(
                    "login.disabled", new Object[0],
                    LocaleContextHolder.getLocale()), ""));
        }

        return oidcUser;
    }
}
