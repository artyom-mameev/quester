package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.test.WithMockCustomUser;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.security.user.service.CustomOidcUserService;
import com.artyommameev.quester.security.user.service.MyUserDetailsService;
import com.artyommameev.quester.service.UserService;
import com.artyommameev.quester.util.AuthenticationChecker;
import com.artyommameev.quester.util.EmailChecker;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.Objects;

import static com.artyommameev.quester.QuesterApplication.MAX_SHORT_STRING_SIZE;
import static com.artyommameev.quester.QuesterApplication.MIN_STRING_SIZE;
import static com.artyommameev.quester.util.test.SimpleStringGenerator.generateSimpleString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(EditProfilePage.class)
@ContextConfiguration(classes = {
        QuesterApplication.class})
@Import({AopAutoConfiguration.class, CurrentUserToModelAspect.class})
public class EditProfilePageTests {
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

    @MockBean
    private PasswordEncoder passwordEncoder;

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
        val mvcResult = mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/login"));
    }

    @Test
    @WithMockCustomUser
    public void getRedirectsToConfirmUsernamePageFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        val mvcResult = mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/confirm-username"));
    }

    @Test
    @WithMockCustomUser
    public void getNotFoundFromOauth2UserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.isOauth2User()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void getOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void getRedirectsToHomeAndLogOutsUserFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        val mvcResult = mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void getContainsUserAttributeFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockCustomUser
    public void getContainsEditProfileDtoAttributeFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/profile"))
                .andExpect(model().attributeExists("editProfileDto"));
    }

    @Test
    public void putRedirectsToLoginFromGuest() throws Exception {
        val mvcResult = mockMvc.perform(put("/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .param("email", "test@test.com")
                .param("password", "")
                .param("matchingPassword", "")
                .param("currentPassword", "password"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/login"));
    }

    @Test
    @WithMockCustomUser
    public void putRedirectsToHomeFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        val mvcResult = mockMvc.perform(put("/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .param("email", "test@test.com")
                .param("password", "")
                .param("matchingPassword", "")
                .param("currentPassword", "password"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
    }

    @Test
    @WithMockCustomUser
    public void putForbiddenFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(put("/profile")
                .with(csrf())
                .param("email", "test@test.com")
                .param("password", "")
                .param("matchingPassword", "")
                .param("currentPassword", "password"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void putForbiddenAndLogOutsUserFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        doThrow(new DisabledException("Test")).when(actualUser).getCurrentUser();

        mockMvc.perform(put("/profile")
                .with(csrf())
                .param("email", "test@test.com")
                .param("password", "password")
                .param("matchingPassword", "password")
                .param("currentPassword", "currentPassword"))
                .andExpect(status().isForbidden()).andReturn();

        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void putCallsUserServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        mockMvc.perform(put("/profile")
                .with(csrf())
                .param("email", "test@test.com")
                .param("password", "newPassword")
                .param("matchingPassword", "newPassword")
                .param("currentPassword", "password"));

        verify(userService, times(1))
                .updateUserAccount(user, "test@test.com",
                        "newPassword", "newPassword");
    }

    @Test
    @WithMockCustomUser
    public void putBadRequestFromUserWithConfirmedUsernameWhenUserServiceThrowsVerificationException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        doThrow(new UserService.VerificationException(new Throwable("Test")))
                .when(userService).updateUserAccount(any(), anyString(),
                anyString(), anyString());

        mockMvc.perform(put("/profile")
                .with(csrf())
                .param("email", "test@test.com")
                .param("password", "newPassword")
                .param("matchingPassword", "newPassword")
                .param("currentPassword", "password"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    public void putBadRequestFromUserWithConfirmedUsernameWhenUserServiceThrowsUserAlreadyExistsException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        doThrow(new UserService.UserAlreadyExistsException("Test"))
                .when(userService).updateUserAccount(any(), anyString(),
                anyString(), anyString());

        mockMvc.perform(put("/profile")
                .with(csrf())
                .param("email", "test@test.com")
                .param("password", "newPassword")
                .param("matchingPassword", "newPassword")
                .param("currentPassword", "password"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    public void putRejectsBlankEmailFromUserWithConfirmedUsername() throws Exception {
        try (MockedStatic<EmailChecker> utilities =
                     Mockito.mockStatic(EmailChecker.class)) {
            utilities.when(() -> EmailChecker.isEmail(any())).thenReturn(true);

            when(actualUser.isLoggedIn()).thenReturn(true);
            when(actualUser.getCurrentUser()).thenReturn(user);
            when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

            mockMvc.perform(put("/profile")
                    .with(csrf())
                    .param("email", "   ")
                    .param("password", "newPassword")
                    .param("matchingPassword", "newPassword")
                    .param("currentPassword", "password"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrorCode(
                            "editProfileDto", "email",
                            "NotBlank"));
        }
    }

    @Test
    @WithMockCustomUser
    public void putRejectsEmailWithIncorrectMinSizeFromUserWithConfirmedUsername() throws Exception {
        try (MockedStatic<EmailChecker> utilities =
                     Mockito.mockStatic(EmailChecker.class)) {
            utilities.when(() -> EmailChecker.isEmail(any())).thenReturn(true);

            when(actualUser.isLoggedIn()).thenReturn(true);
            when(actualUser.getCurrentUser()).thenReturn(user);
            when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

            mockMvc.perform(put("/profile")
                    .with(csrf())
                    .param("email", generateSimpleString(
                            MIN_STRING_SIZE - 1))
                    .param("password", "newPassword")
                    .param("matchingPassword", "newPassword")
                    .param("currentPassword", "password"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrorCode(
                            "editProfileDto", "email",
                            "Size"));
        }
    }

    @Test
    @WithMockCustomUser
    public void putRejectsEmailWithIncorrectMaxSizeFromUserWithConfirmedUsername() throws Exception {
        try (MockedStatic<EmailChecker> utilities =
                     Mockito.mockStatic(EmailChecker.class)) {
            utilities.when(() -> EmailChecker.isEmail(any())).thenReturn(true);

            when(actualUser.isLoggedIn()).thenReturn(true);
            when(actualUser.getCurrentUser()).thenReturn(user);
            when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

            mockMvc.perform(put("/profile")
                    .with(csrf())
                    .param("email", generateSimpleString(
                            MAX_SHORT_STRING_SIZE + 1))
                    .param("password", "newPassword")
                    .param("matchingPassword", "newPassword")
                    .param("currentPassword", "password"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrorCode(
                            "editProfileDto", "email",
                            "Size"));
        }
    }

    @Test
    @WithMockCustomUser
    public void putRejectsWrongFormatEmailFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        mockMvc.perform(put("/profile")
                .with(csrf())
                .param("email",
                        generateSimpleString(MIN_STRING_SIZE))
                .param("password", "newPassword")
                .param("matchingPassword", "newPassword")
                .param("currentPassword", "password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(
                        "editProfileDto", "email",
                        "ValidEmail"));
    }

    @Test
    @WithMockCustomUser
    public void putRejectsBlankCurrentPasswordFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(anyString(), any()))
                .thenReturn(true);

        mockMvc.perform(put("/profile")
                .with(csrf())
                .param("email", "test@test.com")
                .param("password", "newPassword")
                .param("matchingPassword", "newPassword")
                .param("currentPassword", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(
                        "editProfileDto", "currentPassword",
                        "NotBlank"));
    }

    @Test
    @WithMockCustomUser
    public void putRejectsIncorrectCurrentPasswordFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(actualUser.getPassword()).thenReturn("password");
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.matches(eq("incorrectPassword"),
                eq("password"))).thenReturn(false);

        mockMvc.perform(put("/profile")
                .with(csrf())
                .param("email", "test@test.com")
                .param("password", "newPassword")
                .param("matchingPassword", "newPassword")
                .param("currentPassword", "incorrectPassword"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(
                        "editProfileDto", "currentPassword",
                        "CurrentPasswordIsCorrect"));
    }

    @Test
    @WithMockCustomUser
    public void putRejectsNotMatchingNewPasswordFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        mockMvc.perform(put("/profile")
                .with(csrf())
                .param("email", "test@test.com")
                .param("password", "newPassword")
                .param("matchingPassword", "newPassword2")
                .param("currentPassword", "password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(
                        "editProfileDto", "matchingPassword",
                        "PasswordMatches"));
    }

    @Test
    @WithMockCustomUser
    public void putRejectsNewPasswordWithIncorrectMinSizeFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        mockMvc.perform(put("/profile")
                .with(csrf())
                .param("email", "test@test.com")
                .param("password",
                        generateSimpleString(MIN_STRING_SIZE - 1))
                .param("matchingPassword",
                        generateSimpleString(MIN_STRING_SIZE - 1))
                .param("currentPassword", "password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(
                        "editProfileDto", "password",
                        "EditedPasswordSize"));
    }

    @Test
    @WithMockCustomUser
    public void putRejectsExistedEmailFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(userService.emailExists(anyString())).thenReturn(true);
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        mockMvc.perform(put("/profile")
                .with(csrf())
                .param("email", "test@test.com")
                .param("password", "newPassword")
                .param("matchingPassword", "newPassword")
                .param("currentPassword", "password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(
                        "editProfileDto", "email",
                        "NewUserEmailNotExists"));
    }
}
