package com.artyommameev.quester.controller.api;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.dto.CommentDto;
import com.artyommameev.quester.entity.User;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(CommentsApi.class)
@ContextConfiguration(classes = {
        QuesterApplication.class})
public class CommentsApiTests {
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
        mockMvc.perform(post("/api/games/1/comments")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void postOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(post("/api/games/1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void postForbiddenFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        mockMvc.perform(post("/api/games/1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void postForbiddenFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new DisabledException("Test")).when(actualUser).getCurrentUser();

        mockMvc.perform(post("/api/games/1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isForbidden());

        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void postReturnsTrueFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(post("/api/games/1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser
    public void postCallsGameServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(post("/api/games/1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))));

        verify(gameService, times(1))
                .createComment(1L, user, "Test");
    }

    @Test
    @WithMockCustomUser
    public void postNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsGameNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        doThrow(new GameService.GameNotFoundException("Test"))
                .when(gameService).createComment(anyLong(), any(), anyString());

        mockMvc.perform(post("/api/games/1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void postForbiddenFromUserWithConfirmedUsernameWhenGameServiceThrowsForbiddenException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        doThrow(new GameService.ForbiddenException("Test"))
                .when(gameService).createComment(anyLong(), any(), anyString());

        mockMvc.perform(post("/api/games/1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void postBadRequestFromUserWithConfirmedUsernameWhenGameServiceThrowsVerificationException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.VerificationException(new Throwable("Test")))
                .when(gameService).createComment(anyLong(), any(), anyString());

        mockMvc.perform(post("/api/games/1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void putForbiddenFromGuest() throws Exception {
        mockMvc.perform(put("/api/games/1/comments/2")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void putOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(put("/api/games/1/comments/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void putForbiddenFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        mockMvc.perform(put("/api/games/1/comments/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void putForbiddenFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        mockMvc.perform(put("/api/games/1/comments/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isForbidden());

        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void putReturnsTrueFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(put("/api/games/1/comments/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser
    public void putCallsGameServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(put("/api/games/1/comments/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))));

        verify(gameService, times(1))
                .editComment(1L, 2L, user, "Test");
    }

    @Test
    @WithMockCustomUser
    public void putNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsGameNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.GameNotFoundException("Test"))
                .when(gameService).editComment(anyLong(), anyLong(), any(),
                anyString());

        mockMvc.perform(put("/api/games/1/comments/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void putNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsCommentNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.CommentNotFoundException(new Exception("Test")))
                .when(gameService).editComment(anyLong(), anyLong(), any(),
                anyString());

        mockMvc.perform(put("/api/games/1/comments/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void putForbiddenFromUserWithConfirmedUsernameWhenGameServiceThrowsForbiddenException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.ForbiddenException("Test"))
                .when(gameService).editComment(anyLong(), anyLong(), any(),
                anyString());

        mockMvc.perform(put("/api/games/1/comments/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void putBadRequestFromUserWithConfirmedUsernameWhenGameServiceThrowsVerificationException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.VerificationException(new Throwable("Test")))
                .when(gameService).editComment(anyLong(), anyLong(), any(),
                anyString());

        mockMvc.perform(put("/api/games/1/comments/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteForbiddenFromGuest() throws Exception {
        mockMvc.perform(delete("/api/games/1/comments/2")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void deleteOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(delete("/api/games/1/comments/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void deleteForbiddenFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        mockMvc.perform(delete("/api/games/1/comments/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void deleteForbiddenFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        mockMvc.perform(delete("/api/games/1/comments/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void deleteReturnsTrueFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(delete("/api/games/1/comments/1")
                .with(csrf()))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser
    public void deleteCallsGameServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(delete("/api/games/1/comments/2")
                .with(csrf()));

        verify(gameService, times(1))
                .deleteComment(1L, 2L, user);
    }

    @Test
    @WithMockCustomUser
    public void deleteNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsGameNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.GameNotFoundException("Test"))
                .when(gameService).deleteComment(anyLong(), anyLong(), any());

        mockMvc.perform(delete("/api/games/1/comments/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void deleteNotFoundFromUserWithConfirmedUsernameWhenGameServiceThrowsCommentNotFoundException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.CommentNotFoundException(new Exception("Test")))
                .when(gameService).deleteComment(anyLong(), anyLong(), any());

        mockMvc.perform(delete("/api/games/1/comments/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    public void deleteForbiddenFromUserWithConfirmedUsernameWhenGameServiceThrowsForbiddenException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new GameService.ForbiddenException("Test"))
                .when(gameService).deleteComment(anyLong(), anyLong(), any());

        mockMvc.perform(delete("/api/games/1/comments/2")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new CommentDto("Test"))))
                .andExpect(status().isForbidden());
    }
}
