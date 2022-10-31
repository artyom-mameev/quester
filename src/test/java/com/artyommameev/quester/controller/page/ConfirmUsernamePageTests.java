package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.test.WithMockCustomUser;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.security.user.service.CustomOidcUserService;
import com.artyommameev.quester.security.user.service.MyUserDetailsService;
import com.artyommameev.quester.service.UserService;
import com.artyommameev.quester.util.AuthenticationChecker;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.Objects;

import static com.artyommameev.quester.util.test.SimpleStringGenerator.generateSimpleString;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ConfirmUsernamePage.class)
@ContextConfiguration(classes = {
        QuesterApplication.class})
public class ConfirmUsernamePageTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private CustomOidcUserService customOidcUserService;

    @MockBean
    @SuppressWarnings("unused")
    private MyUserDetailsService myUserDetailsService;

    @MockBean
    private UserService userService;

    @MockBean
    private ActualUser actualUser;

    @MockBean
    private User user;

    @Resource
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void getRedirectsToLoginFromGuest() throws Exception {
        val mvcResult = mockMvc.perform(
                get("/confirm-username"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/login"));
    }

    @Test
    @WithMockCustomUser
    public void getRedirectsToHomeFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        val mvcResult = mockMvc.perform(
                get("/confirm-username"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
    }

    @Test
    @WithMockCustomUser
    public void getOkFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        mockMvc.perform(get("/confirm-username"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void getRedirectsToHomeAndLogOutsUserAndFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getGoogleUsername();

        val mvcResult = mockMvc.perform(
                get("/confirm-username"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void getContainsConfirmUsernameDtoAttributeFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        mockMvc.perform(get("/confirm-username"))
                .andExpect(model().attributeExists("confirmUsernameDto"));
    }

    @Test
    public void postRedirectsToLoginFromGuest() throws Exception {
        val mvcResult = mockMvc.perform(
                post("/confirm-username")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("username", "Test"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/login"));
    }

    @Test
    @WithMockCustomUser
    public void postReturnsForbiddenFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(post("/confirm-username")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .param("username", "Test"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void postForbiddenAndLogOutsUserFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getGoogleUsername();

        mockMvc.perform(post("/confirm-username"))
                .andExpect(status().isForbidden()).andReturn();

        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void postRedirectsToHomeFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        val mvcResult = mockMvc.perform(
                post("/confirm-username")
                        .with(csrf())
                        .param("username", "Test"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
    }

    @Test
    @WithMockCustomUser
    public void postCallsUserServiceFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(actualUser.getGoogleEmail()).thenReturn("TestGoogleEmail");

        mockMvc.perform(post("/confirm-username")
                .with(csrf())
                .param("username", "Test"));

        verify(userService, times(1))
                .confirmUsernameOfOauth2User(user, "Test");
    }

    @Test
    @WithMockCustomUser
    public void postBadRequestFromUserWithUnconfirmedUsernameWhenUserServiceThrowsVerificationException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);
        when(actualUser.getGoogleEmail()).thenReturn("TestGoogleEmail");

        doThrow(new UserService.VerificationException(new Throwable("Test")))
                .when(userService).confirmUsernameOfOauth2User(any(),
                anyString());

        mockMvc.perform(post("/confirm-username")
                .with(csrf())
                .param("username", "Test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    public void postBadRequestFromUserWithUnconfirmedUsernameWhenUserServiceThrowsAlreadyConfirmedException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);
        when(actualUser.getGoogleEmail()).thenReturn("TestGoogleEmail");

        doThrow(new UserService.AlreadyConfirmedException(new Throwable("Test")))
                .when(userService).confirmUsernameOfOauth2User(any(), anyString());

        mockMvc.perform(post("/confirm-username")
                .with(csrf())
                .param("username", "Test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    public void postRejectsBlankUsernameFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);
        when(actualUser.getGoogleEmail()).thenReturn("TestGoogleEmail");

        mockMvc.perform(post("/confirm-username")
                .with(csrf())
                .param("username", "   "))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(
                        "confirmUsernameDto", "username",
                        "NotBlank"));
    }

    @Test
    @WithMockCustomUser
    public void postRejectsUsernameWithIncorrectMinSizeFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);
        when(actualUser.getGoogleEmail()).thenReturn("TestGoogleEmail");

        mockMvc.perform(post("/confirm-username")
                .with(csrf())
                .param("username",
                        generateSimpleString(QuesterApplication.MIN_STRING_SIZE - 1)))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(
                        "confirmUsernameDto", "username",
                        "Size"));
    }

    @Test
    @WithMockCustomUser
    public void postRejectsUsernameWithIncorrectMaxSizeFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);
        when(actualUser.getGoogleEmail()).thenReturn("TestGoogleEmail");

        mockMvc.perform(post("/confirm-username")
                .with(csrf())
                .param("username",
                        generateSimpleString(QuesterApplication.MAX_SHORT_STRING_SIZE + 1)))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(
                        "confirmUsernameDto", "username",
                        "Size"));
    }

    @Test
    @WithMockCustomUser
    public void postRejectsAlreadyExistedUsernameFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);
        when(actualUser.getGoogleEmail()).thenReturn("TestGoogleEmail");
        when(userService.usernameExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/confirm-username")
                .with(csrf())
                .param("username",
                        generateSimpleString(QuesterApplication.MIN_STRING_SIZE)))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(
                        "confirmUsernameDto", "username",
                        "UsernameNotExists"));
    }
}
