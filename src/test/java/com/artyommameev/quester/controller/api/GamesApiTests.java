package com.artyommameev.quester.controller.api;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.dto.GameDto;
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
import java.util.Date;

import static com.artyommameev.quester.util.test.JsonMapper.asJsonString;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(GamesApi.class)
@ContextConfiguration(classes = {
        QuesterApplication.class})
public class GamesApiTests {
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

    @Mock
    private Game game;

    @Mock
    private Game.JsonReadyGame jsonReadyGame;

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
    public void postForbiddenFromGuest() throws Exception {
        mockMvc.perform(post("/api/games")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void postOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(game.getId()).thenReturn(2L);
        when(gameService.createGame(anyString(), anyString(), anyString(), any(),
                anyBoolean())).thenReturn(game);

        mockMvc.perform(post("/api/games")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void postForbiddenFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);
        when(game.getId()).thenReturn(2L);
        when(gameService.createGame(anyString(), anyString(), anyString(), any(),
                anyBoolean())).thenReturn(game);

        mockMvc.perform(post("/api/games")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void postForbiddenFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(false);

        doThrow(new DisabledException("Test")).when(actualUser).getCurrentUser();

        mockMvc.perform(post("/api/games")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(status().isForbidden());

        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void postReturnsSavedGameIdFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(game.getId()).thenReturn(2L);
        when(gameService.createGame(anyString(), anyString(), anyString(), any(),
                anyBoolean())).thenReturn(game);

        mockMvc.perform(post("/api/games")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(content().string("2"));
    }

    @Test
    @WithMockCustomUser
    public void postCallsGameServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(game.getId()).thenReturn(2L);
        when(gameService.createGame(anyString(), anyString(), anyString(), any(),
                anyBoolean())).thenReturn(game);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(post("/api/games")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))));

        verify(gameService, times(1))
                .createGame("TestName", "TestDesc",
                        "TestLang", user, false);
    }

    @Test
    @WithMockCustomUser
    public void postBadRequestFromUserWithConfirmedUsernameWhenGameServiceThrowsVerificationException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.VerificationException(new Throwable("Test")))
                .when(gameService).createGame(anyString(), anyString(),
                anyString(), any(), anyBoolean());

        mockMvc.perform(post("/api/games")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putForbiddenFromGuest() throws Exception {
        mockMvc.perform(put("/api/games/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void putOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(put("/api/games/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void putForbiddenFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        mockMvc.perform(put("/api/games/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void putForbiddenFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(false);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        mockMvc.perform(put("/api/games/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(status().isForbidden());

        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void putReturnsTrueFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(put("/api/games/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser
    public void putCallsGameServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(put("/api/games/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))));

        verify(gameService, times(1))
                .editGame(1L, "TestName", "TestDesc",
                        "TestLang", user, true);
    }

    @Test
    @WithMockCustomUser
    public void putNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsGameNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.GameNotFoundException("Test"))
                .when(gameService).editGame(anyLong(), anyString(),
                anyString(), anyString(), any(), anyBoolean());

        mockMvc.perform(put("/api/games/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void putForbiddenFromUserWithConfirmedUsernameWhenGameServiceThrowsForbiddenException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.ForbiddenException("Test"))
                .when(gameService).editGame(anyLong(), anyString(),
                anyString(), anyString(), any(), anyBoolean());

        mockMvc.perform(put("/api/games/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void putBadRequestFromUserWithConfirmedUsernameWhenGameServiceThrowsVerificationException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.VerificationException(new Throwable("Test")))
                .when(gameService).editGame(anyLong(), anyString(),
                anyString(), anyString(), any(), anyBoolean());

        mockMvc.perform(put("/api/games/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameDto("TestName",
                        "TestDesc", "TestLang", true))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteForbiddenFromGuest() throws Exception {
        mockMvc.perform(delete("/api/games/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void deleteOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(delete("/api/games/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void deleteForbiddenFromUserWithUnonfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        mockMvc.perform(delete("/api/games/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void deleteForbiddenFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(false);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        mockMvc.perform(delete("/api/games/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void deleteReturnsTrueFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(delete("/api/games/1")
                .with(csrf()))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser
    public void deleteCallsGameServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(delete("/api/games/1")
                .with(csrf()));

        verify(gameService, times(1))
                .deleteGame(1L, user);
    }

    @Test
    @WithMockCustomUser
    public void deleteNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsGameNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.GameNotFoundException("Test"))
                .when(gameService).deleteGame(anyLong(), any());

        mockMvc.perform(delete("/api/games/1")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void deleteForbiddenFromUserWithConfirmedUsernameWhenGameServiceThrowsForbiddenException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.ForbiddenException("Test"))
                .when(gameService).deleteGame(anyLong(), any());

        mockMvc.perform(delete("/api/games/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getOkFromGuest() throws Exception {
        when(gameService.getGameJson(1)).thenReturn(jsonReadyGame);

        mockMvc.perform(get("/api/games/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void getOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(gameService.getGameJson(1)).thenReturn(jsonReadyGame);

        mockMvc.perform(get("/api/games/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void getForbiddenFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);
        when(gameService.getGameJson(1)).thenReturn(jsonReadyGame);

        mockMvc.perform(get("/api/games/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getReturnsGameJson() throws Exception {
        val jsonReadyGame = new Game.JsonReadyGame(1L, "TestName",
                "TestDesc", "TestLang", null,
                "TestUser", new Date(), 5);

        when(gameService.getGameJson(1L)).thenReturn(jsonReadyGame);

        mockMvc.perform(get("/api/games/1"))
                .andExpect(content().json(asJsonString(jsonReadyGame)));
    }

    @Test
    public void getCallsGameService() throws Exception {
        val jsonReadyGame = new Game.JsonReadyGame(1L, "TestName",
                "TestDesc", "TestLang", null,
                "TestUser", new Date(), 5);

        when(gameService.getGameJson(1L)).thenReturn(jsonReadyGame);

        mockMvc.perform(get("/api/games/1")
                .with(csrf()));

        verify(gameService, times(1))
                .getGameJson(1L);
    }

    @Test
    public void getNotFoundWhenGameServiceThrowsGameNotFoundException() throws Exception {
        doThrow(new GameService.GameNotFoundException("Test"))
                .when(gameService).getGameJson(anyLong());

        mockMvc.perform(get("/api/games/1")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getReturnsForbiddenWhenGameServiceThrowsForbiddenException() throws Exception {
        doThrow(new GameService.ForbiddenException("Test"))
                .when(gameService).getGameJson(anyLong());

        mockMvc.perform(get("/api/games/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
