package com.artyommameev.quester.controller.api;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.test.WithMockCustomUser;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.security.user.service.CustomOidcUserService;
import com.artyommameev.quester.security.user.service.MyUserDetailsService;
import com.artyommameev.quester.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(UsersApi.class)
@ContextConfiguration(classes = {
        QuesterApplication.class})
public class UsersApiTests {

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
    public void postToBanForbiddenFromGuest() throws Exception {
        mockMvc.perform(post("/api/users/1/ban")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void postToBanForbiddenFromNormalUser() throws Exception {
        mockMvc.perform(post("/api/users/1/ban")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void postToBanOkFromAdmin() throws Exception {
        mockMvc.perform(post("/api/users/1/ban")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void postToBanReturnsTrueFromAdmin() throws Exception {
        mockMvc.perform(post("/api/users/1/ban")
                .with(csrf()))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void postToBanCallsUserServiceFromAdmin() throws Exception {
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(post("/api/users/1/ban")
                .with(csrf()));

        verify(userService, times(1))
                .setEnabled(1L, false, user);
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void postToBanNotFoundFromAdminWhenUserServiceThrowsUserNotFoundException() throws Exception {
        doThrow(new UserService.UserNotFoundException("Test"))
                .when(userService).setEnabled(anyLong(), anyBoolean(), any());

        mockMvc.perform(post("/api/users/1/ban")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void postToBanForbiddenFromAdminWhenUserServiceThrowsForbiddenException() throws Exception {
        doThrow(new UserService.ForbiddenException("Test"))
                .when(userService).setEnabled(anyLong(), anyBoolean(), any());

        mockMvc.perform(post("/api/users/1/ban")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteToBanForbiddenFromGuest() throws Exception {
        mockMvc.perform(delete("/api/users/1/ban")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void deleteToBanForbiddenFromNormalUser() throws Exception {
        mockMvc.perform(delete("/api/users/1/ban")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToBanOkFromAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1/ban")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToBanReturnsTrueFromAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1/ban")
                .with(csrf()))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteCallsUserServiceToBanFromAdmin() throws Exception {
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(delete("/api/users/1/ban")
                .with(csrf()));

        verify(userService, times(1))
                .setEnabled(1L, true, user);
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToBanNotFoundFromAdminWhenUserServiceThrowsUserNotFoundException() throws Exception {
        doThrow(new UserService.UserNotFoundException("Test"))
                .when(userService).setEnabled(anyLong(), anyBoolean(), any());

        mockMvc.perform(delete("/api/users/1/ban")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToBanForbiddenFromAdminWhenUserServiceThrowsForbiddenException() throws Exception {
        doThrow(new UserService.ForbiddenException("Test"))
                .when(userService).setEnabled(anyLong(), anyBoolean(), any());

        mockMvc.perform(delete("/api/users/1/ban")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteToGamesForbiddenFromGuest() throws Exception {
        mockMvc.perform(delete("/api/users/1/games")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void deleteToGamesForbiddenFromNormalUser() throws Exception {
        mockMvc.perform(delete("/api/users/1/games")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToGamesOkFromAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1/games")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToGamesReturnsTrueFromAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1/games")
                .with(csrf()))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToGamesCallsUserServiceFromAdmin() throws Exception {
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(delete("/api/users/1/games")
                .with(csrf()));

        verify(userService, times(1))
                .deleteUserGames(1, user);
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToGamesNotFoundFromAdminWhenUserServiceThrowsUserNotFoundException() throws Exception {
        doThrow(new UserService.UserNotFoundException("Test"))
                .when(userService).deleteUserGames(anyLong(), any());

        mockMvc.perform(delete("/api/users/1/games")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToGamesForbiddenFromAdminWhenUserServiceThrowsForbiddenException() throws Exception {
        doThrow(new UserService.ForbiddenException("Test"))
                .when(userService).deleteUserGames(anyLong(), any());

        mockMvc.perform(delete("/api/users/1/games")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteToReviewsForbiddenFromGuest() throws Exception {
        mockMvc.perform(delete("/api/users/1/reviews")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void deleteToReviewsForbiddenFromNormalUser() throws Exception {
        mockMvc.perform(delete("/api/users/1/reviews")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToReviewsOkFromAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1/reviews")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToReviewsReturnsTrueFromAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1/reviews")
                .with(csrf()))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToReviewsCallsUserServiceFromAdmin() throws Exception {
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(delete("/api/users/1/reviews")
                .with(csrf()));

        verify(userService, times(1))
                .deleteUserReviews(1, user);
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToReviewsNotFoundFromAdminWhenUserServiceThrowsUserNotFoundException() throws Exception {
        doThrow(new UserService.UserNotFoundException("Test"))
                .when(userService).deleteUserReviews(anyLong(), any());

        mockMvc.perform(delete("/api/users/1/reviews")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToReviewsForbiddenFromAdminWhenUserServiceThrowsForbiddenException() throws Exception {
        doThrow(new UserService.ForbiddenException("Test"))
                .when(userService).deleteUserReviews(anyLong(), any());

        mockMvc.perform(delete("/api/users/1/reviews")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteToCommentsForbiddenFromGuest() throws Exception {
        mockMvc.perform(delete("/api/users/1/comments")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    public void deleteToCommentsForbiddenFromNormalUser() throws Exception {
        mockMvc.perform(delete("/api/users/1/comments")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToCommentsOkFromAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1/comments")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToCommentsReturnsTrueFromAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/1/comments")
                .with(csrf()))
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToCommentsCallsUserServiceFromAdmin() throws Exception {
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(delete("/api/users/1/comments")
                .with(csrf()));

        verify(userService, times(1))
                .deleteUserComments(1, user);
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToCommentsNotFoundFromAdminWhenUserServiceThrowsUserNotFoundException() throws Exception {
        doThrow(new UserService.UserNotFoundException("Test"))
                .when(userService).deleteUserComments(anyLong(), any());

        mockMvc.perform(delete("/api/users/1/comments")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(admin = true)
    public void deleteToCommentsForbiddenFromAdminWhenUserServiceThrowsForbiddenException() throws Exception {
        doThrow(new UserService.ForbiddenException("Test"))
                .when(userService).deleteUserComments(anyLong(), any());

        mockMvc.perform(delete("/api/users/1/comments")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
