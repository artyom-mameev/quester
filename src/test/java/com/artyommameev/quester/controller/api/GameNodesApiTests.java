package com.artyommameev.quester.controller.api;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.dto.ConditionDto;
import com.artyommameev.quester.dto.gamenode.GameNodeCreationDto;
import com.artyommameev.quester.dto.gamenode.GameNodeEditingDto;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.security.test.WithMockCustomUser;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.security.user.service.CustomOidcUserService;
import com.artyommameev.quester.security.user.service.MyUserDetailsService;
import com.artyommameev.quester.service.GameService;
import com.artyommameev.quester.util.AuthenticationChecker;
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

import static com.artyommameev.quester.util.test.JsonMapper.asJsonString;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(GameNodesApi.class)
@ContextConfiguration(classes = {
        QuesterApplication.class})
public class GameNodesApiTests {
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

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void postForbiddenFromGuest() throws Exception {
        mockMvc.perform(post("/api/games/1/nodes")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void postOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(post("/api/games/1/nodes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void postForbiddenFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        mockMvc.perform(post("/api/games/1/nodes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void postForbiddenFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(false);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        mockMvc.perform(post("/api/games/1/nodes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isForbidden());

        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void postReturnsTrueFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(post("/api/games/1/nodes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser
    public void postCallsGameServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(post("/api/games/1/nodes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))));

        verify(gameService, times(1))
                .createGameNode(1L, user, "TestParentId",
                        "TestId", "TestName", "TestDesc",
                        GameNode.NodeType.ROOM, "TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE);
    }

    @Test
    @WithMockCustomUser
    public void postNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsGameNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.GameNotFoundException("Test"))
                .when(gameService).createGameNode(anyLong(), any(), anyString(),
                anyString(), anyString(), anyString(), any(), anyString(),
                any());

        mockMvc.perform(post("/api/games/1/nodes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void postForbiddenFromUserWithConfirmedUsernameWhenGameServiceThrowsForbiddenException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.ForbiddenException("Test"))
                .when(gameService).createGameNode(anyLong(), any(), anyString(),
                anyString(), anyString(), anyString(), any(), anyString(),
                any());

        mockMvc.perform(post("/api/games/1/nodes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void postBadRequestFromUserWithConfirmedUsernameWhenGameServiceThrowsVerificationException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.VerificationException(new Throwable("Test")))
                .when(gameService).createGameNode(anyLong(), any(), anyString(),
                anyString(), anyString(), anyString(), any(), anyString(),
                any());

        mockMvc.perform(post("/api/games/1/nodes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putForbiddenFromGuest() throws Exception {
        mockMvc.perform(put("/api/games/1/nodes/TestId")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void putOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(put("/api/games/1/nodes/TestId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void putForbiddenFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        mockMvc.perform(put("/api/games/1/nodes/TestId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void putForbiddenFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(false);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        mockMvc.perform(put("/api/games/1/nodes/TestId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isForbidden());

        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void putReturnsTrueFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(put("/api/games/1/nodes/TestId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeCreationDto("TestId",
                        "TestParentId", "TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser
    public void putCallsGameServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(put("/api/games/1/nodes/TestId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeEditingDto("TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))));

        verify(gameService, times(1))
                .editGameNode(1L, "TestId", user, "TestName",
                        "TestDesc", "TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE);
    }

    @Test
    @WithMockCustomUser
    public void putNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsGameNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.GameNotFoundException("Test"))
                .when(gameService).editGameNode(anyLong(), anyString(),
                any(), anyString(), anyString(), anyString(), any());

        mockMvc.perform(put("/api/games/1/nodes/TestId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeEditingDto("TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void putNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsNodeNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.NodeNotFoundException(new Exception("Test")))
                .when(gameService).editGameNode(anyLong(), anyString(),
                any(), anyString(), anyString(), anyString(), any());

        mockMvc.perform(put("/api/games/1/nodes/TestId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeEditingDto("TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void putForbiddenFromUserWithConfirmedUsernameWhenGameServiceThrowsForbiddenException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.ForbiddenException("Test"))
                .when(gameService).editGameNode(anyLong(), anyString(),
                any(), anyString(), anyString(), anyString(), any());

        mockMvc.perform(put("/api/games/1/nodes/TestId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeEditingDto("TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void putBadRequestFromUserWithConfirmedUsernameWhenGameServiceThrowsVerificationException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.VerificationException(new Throwable("Test")))
                .when(gameService).editGameNode(anyLong(), anyString(),
                any(), anyString(), anyString(), anyString(), any());

        mockMvc.perform(put("/api/games/1/nodes/TestId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new GameNodeEditingDto("TestName",
                        "TestDesc", new ConditionDto("TestFlagId",
                        GameNode.Condition.FlagState.ACTIVE),
                        GameNode.NodeType.ROOM))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteForbiddenFromGuest() throws Exception {
        mockMvc.perform(delete("/api/games/1/nodes/TestId")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void deleteOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(delete("/api/games/1/nodes/TestId")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void deleteForbiddenFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        mockMvc.perform(delete("/api/games/1/nodes/TestId")
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

        mockMvc.perform(delete("/api/games/1/nodes/TestId")
                .with(csrf()))
                .andExpect(status().isForbidden());

        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void deleteReturnsTrueFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(delete("/api/games/1/nodes/TestId")
                .with(csrf()))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser
    public void deleteCallsGameServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(delete("/api/games/1/nodes/TestId")
                .with(csrf()));

        verify(gameService, times(1))
                .deleteGameNode(1L, "TestId", user);
    }

    @Test
    @WithMockCustomUser
    public void deleteNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsGameNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.GameNotFoundException("Test"))
                .when(gameService).deleteGameNode(anyLong(), anyString(), any());

        mockMvc.perform(delete("/api/games/1/nodes/TestId")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void deleteNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsNodeNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.NodeNotFoundException(new Throwable("Test")))
                .when(gameService).deleteGameNode(anyLong(), anyString(), any());

        mockMvc.perform(delete("/api/games/1/nodes/TestId")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void deleteForbiddenFromUserWithConfirmedUsernameWhenGameServiceThrowsForbiddenException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.ForbiddenException("Test"))
                .when(gameService).deleteGameNode(anyLong(), anyString(), any());

        mockMvc.perform(delete("/api/games/1/nodes/TestId")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void deleteBadRequestFromUserWithConfirmedUsernameWhenGameServiceThrowsVerificationException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.VerificationException(new Throwable("Test")))
                .when(gameService).deleteGameNode(anyLong(), anyString(), any());

        mockMvc.perform(delete("/api/games/1/nodes/TestId")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
