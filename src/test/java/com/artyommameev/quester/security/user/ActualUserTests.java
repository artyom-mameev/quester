package com.artyommameev.quester.security.user;

import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.service.UserService;
import lombok.val;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActualUserTests {

    @Mock
    private UserService userService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private OAuth2User oAuth2User;

    private User userNormal;
    private User userOauth2;

    private ActualUser actualUser;

    private MockedStatic<SecurityContextHolder> securityContextHolder;

    @Before
    public void setUp() throws Exception {
        actualUser = new ActualUser(userService);

        userNormal = createNormalUser();
        userOauth2 = createOauth2User();

        when(securityContext.getAuthentication()).thenReturn(authentication);

        securityContextHolder =
                Mockito.mockStatic(SecurityContextHolder.class);

        securityContextHolder.when(SecurityContextHolder::getContext)
                .thenReturn(securityContext);

        when(authentication.getPrincipal()).thenReturn(userNormal);
        when(userService.getUser(1L)).thenReturn(userNormal);
    }

    @After
    public void cleanUp() {
        securityContextHolder.close();
    }

    @Test
    public void getUsernameReturnsUsername() {
        assertEquals("TestUsername", actualUser.getUsername());
    }

    @Test
    public void getPasswordReturnsPassword() {
        assertEquals("TestPassword", actualUser.getPassword());
    }

    @Test
    public void getEmailReturnsEmail() {
        assertEquals("test@email.com", actualUser.getEmail());
    }

    @Test
    public void getIdReturnsId() {
        assertEquals(1L, actualUser.getId());
    }

    @Test
    public void getGoogleUsernameReturnsGoogleUsernameIfGoogleUser() throws Exception {
        loginAsGoogleUser();

        assertEquals("TestGoogleUsername", actualUser.getGoogleUsername());
    }

    @Test(expected = RuntimeException.class)
    public void getGoogleUsernameThrowsRuntimeExceptionIfNotGoogleUser() {
        actualUser.getGoogleUsername();
    }

    @Test
    public void getGoogleEmailReturnsGoogleEmailIfGoogleUser() throws Exception {
        loginAsGoogleUser();

        assertEquals("test@gmail.com", actualUser.getGoogleEmail());
    }

    @Test(expected = RuntimeException.class)
    public void getGoogleEmailThrowsRuntimeExceptionIfNotGoogleUser() {
        actualUser.getGoogleEmail();
    }

    @Test
    public void hasUnconfirmedUsernameReturnsTrueIfUsernameNotExists() throws Exception {
        loginAsGoogleUser();

        assertTrue(actualUser.hasUnconfirmedUsername());
    }

    @Test
    public void hasUnconfirmedUsernameReturnsFalseIfUsernameAlreadyExists() {
        assertFalse(actualUser.hasUnconfirmedUsername());
    }

    @Test
    public void getCurrentUserReturnsNullIfUserIsNotLoggedIn() {
        when(authentication.getPrincipal()).thenReturn(null);

        assertNull(actualUser.getCurrentUser());
    }

    @Test
    public void getCurrentUserReturnsOauth2UserIfOauth2User() throws Exception {
        loginAsGoogleUser();

        Assert.assertEquals(userOauth2, actualUser.getCurrentUser());
    }

    @Test
    public void getCurrentUserReturnsNormalUserIfNormalUser() {
        Assert.assertEquals(userNormal, actualUser.getCurrentUser());
    }

    @Test(expected = DisabledException.class)
    public void getCurrentUserThrowsDisabledExceptionIfUserIsDisabled() {
        ReflectionTestUtils.setField(userNormal, "enabled", false);

        actualUser.getCurrentUser();
    }

    @Test
    public void isOauth2UserReturnsTrueIfLoggedInAsOauth2User() {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        assertTrue(actualUser.isOauth2User());
    }

    @Test
    public void isOauth2UserReturnsFalseIfLoggedInAsNormalUser() {
        assertFalse(actualUser.isOauth2User());
    }

    @Test(expected = RuntimeException.class)
    public void isOauth2UserThrowsRuntimeExceptionIfUserIsNotLoggedIn() {
        when(authentication.getPrincipal()).thenReturn(null);

        actualUser.isOauth2User();
    }

    @Test
    public void isNormalUserReturnsTrueIfLoggedInAsNormal2User() {
        assertTrue(actualUser.isNormalUser());
    }

    @Test
    public void isNormalUserReturnsFalseIfLoggedInAsOauth2User() {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        assertFalse(actualUser.isNormalUser());
    }

    @Test(expected = RuntimeException.class)
    public void isNormalUserThrowsRuntimeExceptionIfUserIsNotLoggedIn() {
        when(authentication.getPrincipal()).thenReturn(null);

        actualUser.isNormalUser();
    }

    @Test
    public void isLoggedInReturnsTrueIfLoggedInAsNormalUser() {
        assertTrue(actualUser.isLoggedIn());
    }

    @Test
    public void isLoggedInReturnsTrueIfLoggedInAsOauth2User() {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        assertTrue(actualUser.isLoggedIn());
    }

    @Test
    public void isLoggedInReturnsFalseIfUserIsNotLoggedIn() {
        when(authentication.getPrincipal()).thenReturn(null);

        assertFalse(actualUser.isLoggedIn());
    }

    private void loginAsGoogleUser() throws Exception {
        when(oAuth2User.getAttribute("email"))
                .thenReturn("test@gmail.com");
        when(userService.getGoogleUser("test@gmail.com"))
                .thenReturn(userOauth2);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
    }

    public User createNormalUser() throws Exception {
        val user = new User("TestUsername", "test@email.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "password", "TestPassword");
        ReflectionTestUtils.setField(user, "enabled", true);
        return user;
    }

    public User createOauth2User() throws Exception {
        val user = new User("testName", "test@email.com",
                "testPassword", "testPassword",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        ReflectionTestUtils.setField(user, "username", null);
        ReflectionTestUtils.setField(user, "email", null);

        ReflectionTestUtils.setField(user, "googleUsername",
                "TestGoogleUsername");
        ReflectionTestUtils.setField(user, "googleEmail",
                "test@gmail.com");

        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        ReflectionTestUtils.setField(user, "authorities",
                authorities);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "password", "TestPassword");
        ReflectionTestUtils.setField(user, "enabled", true);
        return user;
    }
}
