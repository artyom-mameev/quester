package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.QuesterApplication;
import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.test.WithMockCustomUser;
import com.artyommameev.quester.security.user.ActualUser;
import com.artyommameev.quester.security.user.service.CustomOidcUserService;
import com.artyommameev.quester.security.user.service.MyUserDetailsService;
import com.artyommameev.quester.util.AuthenticationChecker;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ErrorPage.class)
@ContextConfiguration(classes = {
        QuesterApplication.class})
@Import({AopAutoConfiguration.class, CurrentUserToModelAspect.class})
public class ErrorPageTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private CustomOidcUserService customOidcUserService;

    @MockBean
    @SuppressWarnings("unused")
    private MyUserDetailsService myUserDetailsService;

    @Resource
    private WebApplicationContext webApplicationContext;

    @MockBean
    private ActualUser actualUser;

    @MockBean
    private User user;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void okFromGuest() throws Exception {
        mockMvc.perform(get("/error")).andExpect(status().isOk());
    }

    @Test
    public void okFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        mockMvc.perform(get("/error")).andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void redirectsToHomeAndLogOutsUserFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        val mvcResult = mockMvc.perform(get("/error"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void containsUserAttributeFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/error"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockCustomUser
    public void redirectsToConfirmUsernamePageFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        val mvcResult = mockMvc.perform(get("/error"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/confirm-username"));
    }
}
