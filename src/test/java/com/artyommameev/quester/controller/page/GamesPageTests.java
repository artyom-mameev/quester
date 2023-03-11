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
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(GamesPage.class)
@ContextConfiguration(classes = {
        QuesterApplication.class})
@Import({AopAutoConfiguration.class, CurrentUserToModelAspect.class})
public class GamesPageTests {

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

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Page<Game> page;

    @Value("${quester.page-size}")
    private int pageSize;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockCustomUser
    public void gamesWithNeededParamsRedirectsToConfirmUsernamePageFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        val mvcResult = mockMvc.perform(
                get("/games?page=1&sort=oldest"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/confirm-username"));
    }

    @Test
    public void gamesWithNeededParamsOkFromGuest() throws Exception {
        when(gameService.getPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST)).thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest"))
                .andExpect(status().isOk());
    }

    @Test
    public void gamesWithPartialParamsRedirectsToPageWithAllNeededParams() throws Exception {
        when(gameService.getPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST)).thenReturn(page);

        var mvcResult = mockMvc.perform(get(
                "/games?page=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/games?page=1&sort=newest"));

        mvcResult = mockMvc.perform(get(
                "/games?sort=oldest"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/games?sort=oldest&page=1"));
    }

    @Test
    public void gamesWithoutParamsRedirectsToPageWithAllNeededParams() throws Exception {
        when(gameService.getPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST)).thenReturn(page);

        val mvcResult = mockMvc.perform(get(
                "/games"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/games?page=1&sort=newest"));
    }

    @Test
    @WithMockCustomUser
    public void gamesWithNeededParamsOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(gameService.getPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST)).thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void gamesWithNeededParamsRedirectsToHomeAndLogOutsUserFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        val mvcResult = mockMvc.perform(
                get("/games?page=1&sort=oldest"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    public void gamesWithNeededParamsContainsGeneralAttributesFromGuest() throws Exception {
        when(gameService.getPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST)).thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest"))
                .andExpect(model().attribute("mode",
                        GamesPage.FilterMode.ALL))
                .andExpect(model().attribute("currentPage", 1));
    }

    @Test
    @WithMockCustomUser
    public void gamesWithNeededParamsContainsGeneralAttributesFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(gameService.getPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST)).thenReturn(page);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/games?page=1&sort=oldest"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("mode",
                        GamesPage.FilterMode.ALL))
                .andExpect(model().attribute("currentPage", 1));
    }

    @Test
    public void gamesWithNeededParamsSetsTotalPageAttributeToOneWhenNoContent() throws Exception {
        when(gameService.getPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST)).thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest"))
                .andExpect(model().attribute("totalPages", 1));
    }

    @Test
    public void gamesWithNeededParamsSetsNeededAttributesWhenGamesPageHasContent() throws Exception {
        when(page.hasContent()).thenReturn(true);
        when(page.getTotalPages()).thenReturn(2);
        when(page.getContent()).thenReturn(Collections.emptyList());
        when(gameService.getPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST)).thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest"))
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("totalPages", 2));
    }

    @Test
    public void gamesWithNeededParamsCallsGameService() throws Exception {
        when(gameService.getPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST)).thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest"));

        verify(gameService, times(1))
                .getPublishedGamesPage(0, pageSize,
                        GameService.SortingMode.OLDEST);
    }

    @Test
    @WithMockCustomUser
    public void gamesWithNeededParamsBadRequestWhenGameServiceThrowsIllegalPageValueException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        doThrow(new GameService.IllegalPageValueException("Test"))
                .when(gameService).getPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST);

        mockMvc.perform(get("/games?page=1&sort=oldest"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    public void gamesWithNeededParamsAndUserParamRedirectsToConfirmUsernamePageFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        val mvcResult = mockMvc.perform(
                get("/games?page=1&sort=oldest&user=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/confirm-username"));
    }

    @Test
    public void gamesWithNeededParamsAndUserParamOkFromGuest() throws Exception {
        when(gameService.getUserPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, 1L))
                .thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest&user=1"))
                .andExpect(status().isOk());
    }

    @Test
    public void gamesWithPartialParamsAndUserParamRedirectsToPageWithAllNeededParams() throws Exception {
        when(gameService.getUserPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, 1L))
                .thenReturn(page);

        var mvcResult = mockMvc.perform(
                get("/games?page=1&user=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains(
                "/games?page=1&user=1&sort=newest"));

        mvcResult = mockMvc.perform(get(
                "/games?sort=oldest&user=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains(
                "/games?sort=oldest&user=1&page=1"));
    }

    @Test
    public void gamesWitoutAllNeededParamsAndWithUserParamRedirectsToPageWithAllNeededParams() throws Exception {
        when(gameService.getUserPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, 1L))
                .thenReturn(page);

        val mvcResult = mockMvc.perform(
                get("/games?user=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains(
                "/games?user=1&page=1&sort=newest"));
    }

    @Test
    @WithMockCustomUser
    public void gamesWithNeededParamsAndUserParamOkFromUserWithConfirmedUsernameWithParams() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(gameService.getUserPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, 1L))
                .thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest&user=1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void gamesWithNeededParamsAndUserParamRedirectsToHomeAndLogOutsUserFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        val mvcResult = mockMvc.perform(
                get("/games?page=1&sort=oldest&user=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    public void gamesWithNeededParamsAndUserParamContainsGeneralAttributesFromGuest() throws Exception {
        when(gameService.getUserPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, 1L))
                .thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest&user=1"))
                .andExpect(model().attribute("mode",
                        GamesPage.FilterMode.CREATED_BY_USER))
                .andExpect(model().attribute("currentPage", 1));
    }

    @Test
    @WithMockCustomUser
    public void gamesWithNeededParamsAndUserParamContainsGeneralAttributesFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(gameService.getUserPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, 1L))
                .thenReturn(page);
        when(actualUser.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/games?page=1&sort=oldest&user=1"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("mode",
                        GamesPage.FilterMode.CREATED_BY_USER))
                .andExpect(model().attribute("currentPage", 1));
    }

    @Test
    public void gamesWithNeededParamsAndUserParamSetsTotalPageAttributeToOneWhenNoContent() throws Exception {
        when(gameService.getUserPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, 1L))
                .thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest&user=1"))
                .andExpect(model().attribute("totalPages", 1));
    }

    @Test
    public void gamesWithNeededParamsAndUserParamSetsNeededAttributesWhenGamesPageHasContent() throws Exception {
        when(page.hasContent()).thenReturn(true);
        when(page.getTotalPages()).thenReturn(2);
        when(page.getContent()).thenReturn(Collections.emptyList());
        when(gameService.getUserPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, 1L))
                .thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest&user=1"))
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("totalPages", 2));
    }

    @Test
    public void gamesWithNeededParamsAndUserParamCallsUserService() throws Exception {
        when(gameService.getUserPublishedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, 1L))
                .thenReturn(page);

        mockMvc.perform(get("/games?page=1&sort=oldest&user=1"));

        verify(gameService, times(1))
                .getUserPublishedGamesPage(0, pageSize,
                        GameService.SortingMode.OLDEST, 1L);
    }

    @Test
    @WithMockCustomUser
    public void gamesWithNeededParamsAndUserParamBadRequestWhenUserServiceThrowsIllegalPageValueException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);

        doThrow(new GameService.IllegalPageValueException("Test"))
                .when(gameService).getUserPublishedGamesPage(0,
                pageSize, GameService.SortingMode.OLDEST, 1L);

        mockMvc.perform(get("/games?page=1&sort=oldest&user=1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    public void userFavoritesWithNeededParamsRedirectsToConfirmUsernamePageFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        val mvcResult = mockMvc.perform(
                get("/favorites?page=1&sort=oldest"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/confirm-username"));
    }

    @Test
    public void userFavoritesWithNeededParamsRedirectsToLoginFromGuest() throws Exception {
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserFavoritedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, user))
                .thenReturn(page);

        val mvcResult = mockMvc.perform(
                get("/favorites?page=1&sort=oldest"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/login"));
    }

    @Test
    @WithMockCustomUser
    public void userFavoritesWithNeededParamsOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserFavoritedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, user))
                .thenReturn(page);
        when(user.getId()).thenReturn(1L);

        mockMvc.perform(get("/favorites?page=1&sort=oldest"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void userFavoritesWithNeededParamsRedirectsToHomeAndLogOutsUserFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        val mvcResult = mockMvc.perform(
                get("/favorites?page=1&sort=oldest"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void userFavoritesWithPartialParamsRedirectsToPageWithAllNeededParamsFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserFavoritedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, user))
                .thenReturn(page);
        when(user.getId()).thenReturn(1L);

        var mvcResult = mockMvc.perform(
                get("/favorites?page=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/favorites?page=1&sort=newest"));

        mvcResult = mockMvc.perform(get(
                "/favorites?sort=oldest"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains(
                "/favorites?sort=oldest&page=1"));
    }

    @Test
    @WithMockCustomUser
    public void userFavoritesWithoutParamsRedirectsToPageWithAllNeededParamsFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserFavoritedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, user))
                .thenReturn(page);
        when(user.getId()).thenReturn(1L);

        val mvcResult = mockMvc.perform(get("/favorites"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains(
                "/favorites?page=1&sort=newest"));
    }

    @Test
    @WithMockCustomUser
    public void userFavoritesWithNeededParamsContainsGeneralAttributesFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserFavoritedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, user))
                .thenReturn(page);
        when(user.getId()).thenReturn(1L);

        mockMvc.perform(get("/favorites?page=1&sort=oldest"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("mode",
                        GamesPage.FilterMode.FAVORITED_BY_USER))
                .andExpect(model().attribute("currentPage", 1));
    }

    @Test
    @WithMockCustomUser
    public void userFavoritesWithNeededParamsSetsNeededAttributesFromUserWithConfirmedUsernameWhenGamesPageHasContent() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(page.hasContent()).thenReturn(true);
        when(page.getTotalPages()).thenReturn(2);
        when(page.getContent()).thenReturn(Collections.emptyList());
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(gameService.getUserFavoritedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, user))
                .thenReturn(page);

        mockMvc.perform(get("/favorites?page=1&sort=oldest"))
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("totalPages", 2));
    }

    @Test
    @WithMockCustomUser
    public void userFavoritesWithNeededParamsSetsTotalPageAttributeToOneFromUserWithConfirmedUsernameWhenNoContent() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserFavoritedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, user))
                .thenReturn(page);
        when(user.getId()).thenReturn(1L);

        mockMvc.perform(get("/favorites?page=1&sort=oldest"))
                .andExpect(model().attribute("totalPages", 1));
    }

    @Test
    @WithMockCustomUser
    public void userFavoritesWithNeededParamsCallsUserServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserFavoritedGamesPage(0, pageSize,
                GameService.SortingMode.OLDEST, user))
                .thenReturn(page);
        when(user.getId()).thenReturn(1L);

        mockMvc.perform(get("/favorites?page=1&sort=oldest"));

        verify(gameService, times(1))
                .getUserFavoritedGamesPage(0, pageSize,
                        GameService.SortingMode.OLDEST, user);
    }

    @Test
    @WithMockCustomUser
    public void userFavoritesWithNeededParamsBadRequestFromUserWithConfirmedUsernameWhenUserServiceThrowsIllegalPageValueException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);

        doThrow(new GameService.IllegalPageValueException("Test"))
                .when(gameService).getUserFavoritedGamesPage(0,
                pageSize, GameService.SortingMode.OLDEST, user);

        mockMvc.perform(get("/favorites?page=1&sort=oldest"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    public void gamesInWorkWithPageParamRedirectsToConfirmUsernamePageFromUserWithUnconfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.hasUnconfirmedUsername()).thenReturn(true);

        val mvcResult = mockMvc.perform(
                get("/in-work?page=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/confirm-username"));
    }

    @Test
    public void gamesInWorkWithPageParamRedirectsToLoginFromGuest() throws Exception {
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserNotPublishedGamesPage(0, pageSize,
                user)).thenReturn(page);

        val mvcResult = mockMvc.perform(
                get("/in-work?page=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/login"));
    }

    @Test
    @WithMockCustomUser
    public void gamesInWorkWithPageParamOkFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserNotPublishedGamesPage(0, pageSize,
                user)).thenReturn(page);
        when(user.getId()).thenReturn(1L);

        mockMvc.perform(get("/in-work?page=1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    public void gamesInWorkWithPageParamRedirectsToHomeAndLogOutsUserFromDisabledUser() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);

        doThrow(new DisabledException("Test"))
                .when(actualUser).getCurrentUser();

        val mvcResult = mockMvc.perform(
                get("/in-work?page=1"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/"));
        assertFalse(AuthenticationChecker.isAuthenticated());
    }

    @Test
    @WithMockCustomUser
    public void gamesInWorkWithoutPageParamRedirectsToPageWithPageParamFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserNotPublishedGamesPage(0, pageSize,
                user)).thenReturn(page);
        when(user.getId()).thenReturn(1L);

        val mvcResult = mockMvc.perform(get("/in-work"))
                .andExpect(status().is3xxRedirection()).andReturn();

        assertTrue(Objects.requireNonNull(mvcResult.getResponse()
                .getRedirectedUrl()).contains("/in-work?page=1"));
    }

    @Test
    @WithMockCustomUser
    public void gamesInWorkWithPageParamContainsGeneralAttributesFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserNotPublishedGamesPage(0, pageSize,
                user)).thenReturn(page);
        when(user.getId()).thenReturn(1L);

        mockMvc.perform(get("/in-work?page=1"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("mode",
                        GamesPage.FilterMode.IN_WORK))
                .andExpect(model().attribute("currentPage", 1));
    }

    @Test
    @WithMockCustomUser
    public void gamesInWorkWithPageParamSetsNeededAttributesFromUserWithConfirmedUsernameWhenGamesPageHasContent() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(page.hasContent()).thenReturn(true);
        when(page.getTotalPages()).thenReturn(2);
        when(page.getContent()).thenReturn(Collections.emptyList());
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(gameService.getUserNotPublishedGamesPage(0, pageSize,
                user)).thenReturn(page);

        mockMvc.perform(get("/in-work?page=1"))
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("totalPages", 2));
    }

    @Test
    @WithMockCustomUser
    public void gamesInWorkWithPageParamSetsTotalPageAttributeToOneFromUserWithConfirmedUsernameWhenNoContent() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserNotPublishedGamesPage(0, pageSize,
                user)).thenReturn(page);
        when(user.getId()).thenReturn(1L);

        mockMvc.perform(get("/in-work?page=1"))
                .andExpect(model().attribute("totalPages", 1));
    }

    @Test
    @WithMockCustomUser
    public void gamesInWorkWithPageParamCallsUserServiceFromUserWithConfirmedUsername() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(gameService.getUserNotPublishedGamesPage(0, pageSize,
                user)).thenReturn(page);
        when(user.getId()).thenReturn(1L);

        mockMvc.perform(get("/in-work?page=1"));

        verify(gameService, times(1))
                .getUserNotPublishedGamesPage(0, pageSize,
                        user);
    }

    @Test
    @WithMockCustomUser
    public void gamesInWorkWithPageParamBadRequestFromUserWithConfirmedUsernameWhenUserServiceThrowsIllegalPageValueException() throws Exception {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);

        doThrow(new GameService.IllegalPageValueException("Test"))
                .when(gameService).getUserNotPublishedGamesPage(0,
                pageSize, user);

        mockMvc.perform(get("/in-work?page=1"))
                .andExpect(status().isBadRequest());
    }
}
