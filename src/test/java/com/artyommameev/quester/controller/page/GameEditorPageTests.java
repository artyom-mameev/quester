package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.test.WithMockCustomUser;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.security.user.service.CustomOidcUserService;
import com.artyommameev.quester.security.user.service.MyUserDetailsService;
import com.artyommameev.quester.service.GameService;
import com.artyommameev.quester.util.AuthenticationChecker;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.Objects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(GameEditorPage.class)
@ContextConfiguration(classes = {
        QuesterApplication.class})
@Import({AopAutoConfiguration.class, CurrentUserToModelAspect.class})
public class GameEditorPageTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private CustomOidcUserService customOidcUserService;

    @MockBean
    @SuppressWarnings("unused")
    private MyUserDetailsService myUserDetailsService;

    @MockBean
    private GameService gameService;

    @MockBean
    private ActualUser actualUser;

    @MockBean
    private User user;

    @Resource
    private WebApplicationContext webApplicationContext;

    @Mock
    private Game game;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void redirectsToLoginFromGuest() throws Exception {
        val mvcResult = mockMvc.perform(get("/games/1/edit"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/login"));
    }

    @Test
    @WithMockCustomUser
    public void redirectsToConfirmUsernamePageFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        val mvcResult = mockMvc.perform(get("/games/1/edit"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/confirm-username"));
    }

    @Test
    @WithMockCustomUser
    public void okFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getGameForEdit(1L, user)).thenReturn(game);

        mockMvc.perform(get("/games/1/edit"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void redirectsToHomeAndLogOutsUserFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        val mvcResult = mockMvc.perform(get("/games/1/edit"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void containsNeededAttributesFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getGameForEdit(1L, user)).thenReturn(game);

        mockMvc.perform(get("/games/1/edit"))
                .andExpect(model().attributeExists("user", "game",
                        "roomIconUrl", "choiceIconUrl", "flagIconUrl",
                        "conditionIconUrl"));
    }

    @Test
    @WithMockCustomUser
    public void callsGameServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getGameForEdit(1L, user)).thenReturn(game);

        mockMvc.perform(get("/games/1/edit"));

        verify(gameService, times(1))
                .getGameForEdit(1, user);
    }

    @Test
    @WithMockCustomUser
    public void notFoundFromUserWithConfirmedUsernameWhenUserServiceThrowsGameNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        doThrow(new GameService.GameNotFoundException("Test"))
                .when(gameService).getGameForEdit(anyLong(), any());

        mockMvc.perform(get("/games/1/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void notFoundFromUserWithConfirmedUsernameWhenUserServiceThrowsForbiddenException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        doThrow(new GameService.ForbiddenException("Test"))
                .when(gameService).getGameForEdit(anyLong(), any());

        mockMvc.perform(get("/games/1/edit"))
                .andExpect(status().isForbidden());
    }
}
