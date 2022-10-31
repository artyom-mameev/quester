package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.test.WithMockCustomUser;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.security.user.service.CustomOidcUserService;
import com.artyommameev.quester.security.user.service.MyUserDetailsService;
import com.artyommameev.quester.service.CommentService;
import com.artyommameev.quester.util.AuthenticationChecker;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Objects;

import static com.artyommameev.quester.QuesterApplication.PAGE_SIZE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(UserCommentsPage.class)
@ContextConfiguration(classes = {
        QuesterApplication.class})
@Import({AopAutoConfiguration.class, CurrentUserToModelAspect.class})
public class UserCommentsPageTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private CustomOidcUserService customOidcUserService;

    @MockBean
    @SuppressWarnings("unused")
    private MyUserDetailsService myUserDetailsService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private ActualUser actualUser;

    @MockBean
    private User user;

    @Resource
    private WebApplicationContext webApplicationContext;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Page<Comment> page;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        when(actualUser.getId()).thenReturn(1L);
    }

    @Test
    @WithMockCustomUser
    public void withNeededParamsRedirectsToConfirmUsernamePageFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        val mvcResult = mockMvc.perform(get("/comments?page=1&user=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/confirm-username"));
    }

    @Test
    public void withNeededParamsOkFromGuest() throws Exception {
        when(commentService.getUserCommentsPage(1L, PAGE_SIZE, 0))
                .thenReturn(page);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/comments?page=1&user=1"))
                .andExpect(status().isOk());
    }

    @Test
    public void withNeededParamsOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(commentService.getUserCommentsPage(1L, PAGE_SIZE, 0))
                .thenReturn(page);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/comments?page=1&user=1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void withNeededParamsRedirectsToHomeAndLogOutsUserFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        val mvcResult = mockMvc.perform(get("/comments?page=1&user=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    public void withNeededParamsContainsGeneralAttributesFromGuestWhenHasContent() throws Exception {
        when(page.getContent()).thenReturn(Collections.emptyList());
        when(page.hasContent()).thenReturn(true);
        when(page.getTotalPages()).thenReturn(2);
        when(commentService.getUserCommentsPage(1L, PAGE_SIZE, 0))
                .thenReturn(page);

        mockMvc.perform(get("/comments?page=1&user=1"))
                .andExpect(model().attributeExists("comments"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 2));
    }

    @Test
    @WithMockCustomUser
    public void withNeededParamsContainsGeneralAttributesFromUserWithConfirmedUsernameWhenHasContent() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(page.getContent()).thenReturn(Collections.emptyList());
        when(page.hasContent()).thenReturn(true);
        when(page.getTotalPages()).thenReturn(2);
        when(commentService.getUserCommentsPage(1L, PAGE_SIZE, 0))
                .thenReturn(page);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/comments?page=1&user=1"))
                .andExpect(model().attributeExists("comments", "user"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 2));
    }

    @Test
    @WithMockCustomUser
    public void withNeededParamsContainsGeneralAttributesWhenNotContent() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(page.getContent()).thenReturn(Collections.emptyList());
        when(page.hasContent()).thenReturn(true);
        when(page.getTotalPages()).thenReturn(2);
        when(commentService.getUserCommentsPage(1L, PAGE_SIZE, 0))
                .thenReturn(page);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/comments?page=1&user=1"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 2));
    }

    @Test
    public void withNeededParamsCallsCommentService() throws Exception {
        when(commentService.getUserCommentsPage(1L, PAGE_SIZE, 0))
                .thenReturn(page);

        mockMvc.perform(get("/comments?page=1&user=1"));

        verify(commentService, times(1))
                .getUserCommentsPage(1L, PAGE_SIZE, 0);
    }

    @Test
    public void withPartialParamsRedirectsToPageWithAllNeededParams() throws Exception {
        when(commentService.getUserCommentsPage(1L, PAGE_SIZE, 0))
                .thenReturn(page);

        var mvcResult = mockMvc.perform(
                get("/comments?page=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/comments?page=1&user=1"));

        mvcResult = mockMvc.perform(
                get("/comments?user=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/comments?user=1&page=1"));
    }

    @Test
    public void withoutParamsRedirectsToPageWithAllNeededParams() throws Exception {
        when(commentService.getUserCommentsPage(1L, PAGE_SIZE, 0))
                .thenReturn(page);

        val mvcResult = mockMvc.perform(
                get("/comments"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/comments?page=1&user=1"));
    }

    @Test
    @WithMockCustomUser
    public void withNeededParamsBadRequestFromUserWithConfirmedUsernameWhenCommentServiceThrowsIllegalPageValueException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        doThrow(new CommentService.IllegalPageValueException("Test"))
                .when(commentService).getUserCommentsPage(1L,
                PAGE_SIZE, 0);

        mockMvc.perform(get("/comments?page=1&user=1"))
                .andExpect(status().isBadRequest());
    }
}
