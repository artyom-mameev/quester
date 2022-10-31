package com.artyommameev.quester.security.test;

import com.artyommameev.quester.entity.User;
import lombok.val;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.Collections;

/**
 * A custom user security context factory that creates a Spring Security
 * context with a mock {@link User}. Used in the tests.
 *
 * @author Artyom Mameev
 */
public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {

    /**
     * Creates custom user security context with mock {@link User} with the
     * following details:
     * <p>
     * Id: 1;<br>
     * Username: defined by the {@link WithMockCustomUser} annotation;<br>
     * Password: 'password' (encoded by {@link BCryptPasswordEncoder});<br>
     * Email: 'test@test.com';<br>
     * Roles: defined by the {@link WithMockCustomUser} annotation
     * (if not admin, 'ROLE_USER', if admin, 'ROLE_USER' and 'ROLE_ADMIN').
     **/
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        val context = SecurityContextHolder.createEmptyContext();

        User principal;
        try {
            principal = new User(customUser.username(), "test@test.com",
                    "password", "password",
                    new BCryptPasswordEncoder(), (customUser.admin() ?
                    Arrays.asList("ROLE_USER", "ROLE_ADMIN") :
                    Collections.singletonList("ROLE_USER")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try { //set database id using reflection
            FieldUtils.writeField(principal, "id", 1L,
                    true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        val auth = new UsernamePasswordAuthenticationToken(principal,
                "password", principal.getAuthorities());

        context.setAuthentication(auth);

        return context;
    }
}
