package com.artyommameev.quester.util;

import com.artyommameev.quester.entity.User;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A simple utility class used to check authentication status of the current
 * {@link User}.
 *
 * @author Artyom Mameev
 */
@UtilityClass
public class AuthenticationChecker {

    /**
     * Checks if the current {@link User} is authenticated or not.
     *
     * @return true if the {@link User} is authenticated, otherwise false.
     */
    public static boolean isAuthenticated() {
        val authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null || AnonymousAuthenticationToken.class.
                isAssignableFrom(authentication.getClass())) {
            return false;
        }

        return authentication.isAuthenticated();
    }
}
