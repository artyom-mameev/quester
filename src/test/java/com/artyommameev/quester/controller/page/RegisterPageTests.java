package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.security.test.WithMockCustomUser;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.security.user.service.CustomOidcUserService;
import com.artyommameev.quester.security.user.service.MyUserDetailsService;
import com.artyommameev.quester.service.UserService;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.Objects;

import static com.artyommameev.quester.util.test.SimpleStringGenerator.generateSimpleString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(RegisterPage.class)
@ContextConfiguration(classes = {
        QuesterApplication.class})
@Import({AopAutoConfiguration.class, CurrentUserToModelAspect.class})
public class RegisterPageTests {
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
    public void getOkFromGuest() throws Exception {
        mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void getRedirectsToHomePageWhenAuthenticated() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(false);

        val mvcResult = mockMvc.perform(get("/register"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
    }

    @Test
    public void getContainsRegisterUserDtoAttributeFromGuest() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(model().attributeExists("registerUserDto"));
    }

    @Test
    @WithMockCustomUser
    public void postForbiddenWhenAuthenticated() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(false);

        mockMvc.perform(post("/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .param("username", "Username")
                .param("password", "Password")
                .param("matchingPassword", "Password")
                .param("email", "test@test.ru"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void postCallsUserServiceFromGuest() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .param("username", "Username")
                .param("password", "Password")
                .param("matchingPassword", "Password")
                .param("email", "test@test.ru"));

        verify(userService, times(1))
                .registerNewUserAccount("Username", "Password",
                        "Password", "test@test.ru");
    }

    @Test
    public void postBadRequestFromGuestWhenUserServiceThrowsVerificationException() throws Exception {
        doThrow(new UserService.VerificationException(new Throwable("Test")))
                .when(userService).registerNewUserAccount(anyString(),
                anyString(), anyString(), anyString());

        mockMvc.perform(post("/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .param("username", "Username")
                .param("password", "Password")
                .param("matchingPassword", "Password")
                .param("email", "test@test.ru"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postBadRequestFromGuestWhenUserServiceThrowsUserAlreadyExistsException() throws Exception {
        doThrow(new UserService.UserAlreadyExistsException("Test"))
                .when(userService).registerNewUserAccount(anyString(),
                anyString(), anyString(), anyString());

        mockMvc.perform(post("/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .param("username", "Username")
                .param("password", "Password")
                .param("matchingPassword", "Password")
                .param("email", "test@test.ru"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void postRejectsBlankUsernameFromGuest() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "   ")
                .param("password", "Password")
                .param("matchingPassword", "Password")
                .param("email", "test@test.ru"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "registerUserDto", "username",
                        "NotBlank"));
    }

    @Test
    public void postRejectsUsernameWithIncorrectMinSizeFromGuest() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username",
                        generateSimpleString(QuesterApplication.MIN_STRING_SIZE - 1))
                .param("password", "Password")
                .param("matchingPassword", "Password")
                .param("email", "test@test.ru"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "registerUserDto", "username",
                        "Size"));
    }

    @Test
    public void postRejectsUsernameWithIncorrectMaxSizeFromGuest() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username",
                        generateSimpleString(QuesterApplication.MAX_SHORT_STRING_SIZE + 1))
                .param("password", "Password")
                .param("matchingPassword", "Password")
                .param("email", "test@test.ru"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "registerUserDto", "username",
                        "Size"));
    }

    @Test
    public void postRejectsExistsUsernameFromGuest() throws Exception {
        when(userService.usernameExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "Username")
                .param("password", "Password")
                .param("matchingPassword", "Password")
                .param("email", "test@test.ru"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "registerUserDto", "username",
                        "UsernameNotExists"));
    }

    @Test
    public void postRejectsBlankPasswordFromGuest() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "Username")
                .param("password", "   ")
                .param("matchingPassword", "Password")
                .param("email", "test@test.ru"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "registerUserDto", "password",
                        "NotBlank"));
    }

    @Test
    public void postRejectsPasswordWithIncorrectMinSizeFromGuest() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "Username")
                .param("password",
                        generateSimpleString(QuesterApplication.MIN_STRING_SIZE - 1))
                .param("matchingPassword", "Password")
                .param("email", "test@test.ru"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "registerUserDto", "password",
                        "Size"));
    }

    @Test
    public void postRejectsPasswordWithIncorrectMaxSizeFromGuest() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "Username")
                .param("password",
                        generateSimpleString(QuesterApplication.MAX_SHORT_STRING_SIZE + 1))
                .param("matchingPassword", "Password")
                .param("email", "test@test.ru"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "registerUserDto", "password",
                        "Size"));
    }

    @Test
    public void postRejectsNotMatchingPasswordFromGuest() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "Username")
                .param("password", "Password")
                .param("matchingPassword", "Password2")
                .param("email", "test@test.ru"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "registerUserDto", "matchingPassword",
                        "PasswordMatches"));
    }

    @Test
    public void postRejectsBlankMatchingPasswordFromGuest() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "Username")
                .param("password", "Password")
                .param("matchingPassword", "Password2")
                .param("email", "test@test.ru"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "registerUserDto", "matchingPassword",
                        "PasswordMatches"));
    }

    @Test
    public void postRejectsBlankEmailFromGuest() throws Exception {
        try (MockedStatic<EmailChecker> utilities =
                     Mockito.mockStatic(EmailChecker.class)) {
            utilities.when(() -> EmailChecker.isEmail(any())).thenReturn(true);

            mockMvc.perform(post("/register")
                    .with(csrf())
                    .param("username", "Username")
                    .param("password", "Password")
                    .param("matchingPassword", "Password")
                    .param("email", "   "))
                    .andExpect(model().attributeHasFieldErrorCode(
                            "registerUserDto", "email",
                            "NotBlank"));
        }
    }

    @Test
    public void postRejectsEmailWithIncorrectMinSizeFromGuest() throws Exception {
        try (MockedStatic<EmailChecker> utilities =
                     Mockito.mockStatic(EmailChecker.class)) {
            utilities.when(() -> EmailChecker.isEmail(any())).thenReturn(true);

            mockMvc.perform(post("/register")
                    .with(csrf())
                    .param("username", "Username")
                    .param("password", "Password")
                    .param("matchingPassword", "Password")
                    .param("email",
                            generateSimpleString(QuesterApplication.MIN_STRING_SIZE - 1)))
                    .andExpect(model().attributeHasFieldErrorCode(
                            "registerUserDto", "email",
                            "Size"));
        }
    }

    @Test
    public void postRejectsEmailWithIncorrectMaxSizeFromGuest() throws Exception {
        try (MockedStatic<EmailChecker> utilities =
                     Mockito.mockStatic(EmailChecker.class)) {
            utilities.when(() -> EmailChecker.isEmail(any())).thenReturn(true);

            mockMvc.perform(post("/register")
                    .with(csrf())
                    .param("username", "Username")
                    .param("password", "Password")
                    .param("matchingPassword", "Password")
                    .param("email",
                            generateSimpleString(QuesterApplication.MAX_SHORT_STRING_SIZE + 1)))
                    .andExpect(model().attributeHasFieldErrorCode(
                            "registerUserDto", "email",
                            "Size"));
        }
    }

    @Test
    public void postRejectsWrongEmailFormatFromGuest() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "Username")
                .param("password", "Password")
                .param("matchingPassword", "Password")
                .param("email", "Email"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "registerUserDto", "email",
                        "ValidEmail"));
    }

    @Test
    public void postRejectsAlreadyExistedEmailFromGuest() throws Exception {
        when(userService.emailExists(anyString())).thenReturn(true);

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "Username")
                .param("password", "Password")
                .param("matchingPassword", "Password")
                .param("email", "test@test.com"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "registerUserDto", "email",
                        "NewUserEmailNotExists"));
    }
}
