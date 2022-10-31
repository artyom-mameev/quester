package com.artyommameev.quester.util;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationCheckerTests {

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @Test
    public void checksAuthentication() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            assertTrue(AuthenticationChecker.isAuthenticated());

            when(authentication.isAuthenticated()).thenReturn(false);

            assertFalse(AuthenticationChecker.isAuthenticated());
        }
    }

    @Test
    public void returnsFalseIfAuthenticationIsNull() {
        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            assertFalse(AuthenticationChecker.isAuthenticated());
        }
    }

    @Test
    public void returnsFalseIfAuthenticationIsAnonymousAuthenticationToken() {
        val authentication =
                mock(AnonymousAuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> securityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {
            securityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            assertFalse(AuthenticationChecker.isAuthenticated());
        }
    }
}
