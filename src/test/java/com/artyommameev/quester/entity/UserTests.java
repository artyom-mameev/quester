package com.artyommameev.quester.entity;

import com.artyommameev.quester.entity.gamenode.GameNode;
import com.artyommameev.quester.entity.gamenode.RoomNode;
import lombok.val;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("ConstantConditions")
public class UserTests {

    private User user;

    @Mock
    private OidcUser oidcUser;
    @Mock
    private Game unpublishedGame;
    @Mock
    private Game publishedGame;

    @Before
    public void setUp() throws Exception {
        user = makeNormalUser();

        when(publishedGame.isPublished()).thenReturn(true);
    }

    @Test(expected = User.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfUsernameIsNull() throws Exception {
        new User(null, "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));
    }

    @Test(expected = User.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfEmailIsNull() throws Exception {
        new User("name", null,
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));
    }

    @Test(expected = User.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfPasswordIsNull() throws Exception {
        new User("name", "email@test.com",
                null, "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));
    }

    @Test(expected = User.NullValueException.class)
    public void constructorThrowsNullValueExceptionIfMatchingPasswordIsNull() throws Exception {
        new User("name", "email@test.com",
                "password", null,
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsNullPointerExceptionIfPasswordEncoderIsNull() throws Exception {
        new User("name", "email@test.com",
                "password", "password",
                null,
                Collections.singletonList("ROLE_USER"));
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsNullPointerExceptionIfAuthoritiesIsNull() throws Exception {
        new User("name", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                null);
    }

    @Test
    public void constructorTrimsUsername() throws Exception {
        val user = new User(" Username ", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        assertEquals("Username", user.getUsername());
    }

    @Test(expected = User.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfUsernameIsEmpty() throws Exception {
        new User(" ", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));
    }

    @Test(expected = User.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfPasswordIsEmpty() throws Exception {
        new User("Username", "email@test.com",
                "", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));
    }

    @Test(expected = User.EmptyStringException.class)
    public void constructorThrowsEmptyStringExceptionIfMatchingPasswordIsEmpty() throws Exception {
        new User("Username", "email@test.com",
                "password", "",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));
    }

    @Test(expected = User.PasswordsNotMatchException.class)
    public void constructorThrowsPasswordsNotMatchExceptionIfPasswordIsNotEquals() throws Exception {
        new User("Username", "email@test.com",
                "password", "password1",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));
    }

    @Test
    public void constructorSetsPassword() throws Exception {
        val user = new User("Username", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        assertNotNull(user.getPassword());
    }

    @Test(expected = User.EmailFormatException.class)
    public void constructorThrowsEmailFormatExceptionIfEmailInWrongFormat() throws Exception {
        new User("Username", "wrongFormat",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));
    }

    @Test
    public void constructorSetsAndTrimsEmail() throws Exception {
        val user = new User("Username", " correct@test.com ",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        assertEquals("correct@test.com", user.getEmail());
    }

    @Test(expected = User.NoAuthoritiesException.class)
    public void constructorThrowsNoAuthoritiesExceptionIfAuthoritiesListIsEmpty() throws Exception {
        new User("Username", " correct@test.com ",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.emptyList());
    }

    @Test(expected = User.EmptyAuthorityException.class)
    public void constructorThrowsEmptyAuthorityExceptionIfAuthoritiesListHasEmptyRole() throws Exception {
        new User("Username", " correct@test.com ",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList(""));
    }

    @Test
    public void constructorSetsAuthorities() throws Exception {
        val authorities = new ArrayList<String>();
        Collections.addAll(authorities, "ROLE_USER", "ROLE_ADMIN");

        val user = new User("Username", " correct@test.com ",
                "password", "password",
                new BCryptPasswordEncoder(), authorities);

        assertTrue(user.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(user.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    public void constructorCreatesUserWithoutRestrictions() {
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test(expected = NullPointerException.class)
    public void fromOidcUserThrowsNullPointerExceptionIfOidcUserIsNull() {
        User.fromGoogleOidcUser(null);
    }


    @Test(expected = NullPointerException.class)
    public void fromOidcUserThrowsNullPointerExceptionIfOidcUserNameAttributeIsNull() {
        when(oidcUser.getAttribute("name")).thenReturn(null);

        User.fromGoogleOidcUser(oidcUser);
    }

    @Test(expected = NullPointerException.class)
    public void fromOidcUserThrowsNullPointerExceptionIfOidcUserEmailAttributeIsNull() {
        when(oidcUser.getAttribute("name")).thenReturn("name");
        when(oidcUser.getAttribute("email")).thenReturn(null);

        User.fromGoogleOidcUser(oidcUser);
    }

    @Test(expected = User.NotGoogleEmailException.class)
    public void fromOidcUserThrowsNotGoogleEmailExceptionIfEmailIsNotGoogleEmail() {
        when(oidcUser.getAttribute("name")).thenReturn("name");
        when(oidcUser.getAttribute("email")).thenReturn("email@test.com");

        User.fromGoogleOidcUser(oidcUser);
    }

    @Test
    public void fromOidcUserSetsUserAuthority() {
        when(oidcUser.getAttribute("name")).thenReturn("name");
        when(oidcUser.getAttribute("email")).thenReturn("test@gmail.com");

        val user = User.fromGoogleOidcUser(oidcUser);

        assertEquals(1, user.getAuthorities().size());
        assertTrue(user.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    public void fromOidcUserCreatesUserWithoutRestrictions() {
        when(oidcUser.getAttribute("name")).thenReturn("name");
        when(oidcUser.getAttribute("email")).thenReturn("test@gmail.com");

        val user = User.fromGoogleOidcUser(oidcUser);

        assertTrue(user.isEnabled());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isAccountNonExpired());
    }

    @Test(expected = User.NullValueException.class)
    public void confirmUsernameThrowsNullValueExceptionIfUsernameIsNull() throws Exception {
        val user = new User();

        user.confirmUsername(null);
    }

    @Test
    public void confirmUsernameTrimsUsername() throws Exception {
        val user = new User();

        user.confirmUsername(" Username ");

        assertEquals("Username", user.getUsername());
    }

    @Test(expected = User.EmptyStringException.class)
    public void confirmUsernameThrowsEmptyStringExceptionIfUsernameIsEmpty() throws Exception {
        val user = new User();

        user.confirmUsername(" ");
    }

    @Test(expected = User.UsernameExistsException.class)
    public void confirmUsernameThrowsUsernameExistsExceptionIfUsernameAlreadySet() throws Exception {
        user.confirmUsername(" Username ");
    }

    @Test(expected = User.NullValueException.class)
    public void updateUsernameThrowsNullValueExceptionIfUsernameIsNull() throws Exception {
        user.updateUsername(null);
    }

    @Test
    public void updateUsernameTrimsUsername() throws Exception {
        user.updateUsername(" Username ");

        assertEquals("Username", user.getUsername());
    }

    @Test(expected = User.EmptyStringException.class)
    public void updateUsernameThrowsEmptyStringExceptionIfUsernameIsEmpty() throws Exception {
        user.updateUsername(" ");
    }

    @Test(expected = User.NullValueException.class)
    public void updatePasswordThrowsNullValueExceptionIfPasswordIsNull() throws Exception {
        user.updatePassword(null, "password",
                new BCryptPasswordEncoder());
    }

    @Test(expected = User.NullValueException.class)
    public void updatePasswordThrowsNullValueExceptionIfMatchingPasswordIsNull() throws Exception {
        user.updatePassword("password", null,
                new BCryptPasswordEncoder());
    }

    @Test(expected = NullPointerException.class)
    public void updatePasswordThrowsNullPointerExceptionIfPasswordEncoderIsNull() throws Exception {
        user.updatePassword("password", "password",
                null);
    }

    @Test
    public void updatePasswordThrowsEmptyStringExceptionIfPasswordIsEmpty() {
        assertThrows(User.EmptyStringException.class, () ->
                user.updatePassword("", "password",
                        new BCryptPasswordEncoder()));
        assertThrows(User.EmptyStringException.class, () ->
                user.updatePassword("password", "",
                        new BCryptPasswordEncoder()));
        assertThrows(User.EmptyStringException.class, () ->
                user.updatePassword("", "",
                        new BCryptPasswordEncoder()));
    }

    @Test(expected = User.PasswordsNotMatchException.class)
    public void updatePasswordThrowsPasswordsNotMatchExceptionIsPasswordsNotEqual() throws Exception {
        user.updatePassword("password1",
                "password", new BCryptPasswordEncoder());
    }

    @Test
    public void updatePasswordUpdatesPassword() throws Exception {
        val oldPassword = user.getPassword();

        user.updatePassword("newPassword", "newPassword",
                new BCryptPasswordEncoder());

        assertNotEquals(oldPassword, user.getPassword());
    }

    @Test(expected = User.NullValueException.class)
    public void updateEmailThrowsNullValueExceptionIfEmailIsNull() throws Exception {
        user.updateEmail(null);
    }

    @Test(expected = User.EmailFormatException.class)
    public void updateEmailThrowsEmailFormatExceptionIfEmailHasWrongFormat() throws Exception {
        user.updateEmail("wrongFormat");
    }

    @Test
    public void updateEmailTrimsEmail() throws Exception {
        user.updateEmail(" correct@test.com ");

        assertEquals("correct@test.com", user.getEmail());
    }

    @Test
    public void getAuthoritiesReturnsUnmodifiableList() {
        val authorities = user.getAuthorities();

        assertThrows(UnsupportedOperationException.class, () ->
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThrows(UnsupportedOperationException.class, () ->
                authorities.remove(1));

        assertThrows(UnsupportedOperationException.class, authorities::clear);
    }

    @Test
    public void isAdminReturnsTrueIfUserIsAdmin() throws Exception {
        val admin = new User("username", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_ADMIN"));

        assertTrue(admin.isAdmin());
    }

    @Test
    public void isAdminReturnsFalseIfUserIsNotAdmin() {
        assertFalse(user.isAdmin());
    }

    @Test(expected = NullPointerException.class)
    public void clearGamesThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        user.clearGames(null);
    }

    @Test
    public void clearGamesClearsGamesFromAdmin() throws Exception {
        val user = new User("username", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_ADMIN"));

        val games = mock(List.class);

        ReflectionTestUtils.setField(user, "games", games);

        user.clearGames(user);

        verify(games, times(1)).clear();
    }

    @Test(expected = User.ForbiddenManipulationException.class)
    public void clearGamesThrowsForbiddenManipulationExceptionIfUserIsNotAdmin() throws Exception {
        user.clearGames(user);
    }

    @Test(expected = NullPointerException.class)
    public void clearReviewsThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        user.clearReviews(null);
    }

    @Test
    public void clearReviewsClearsReviewsFromAdmin() throws Exception {
        val user = new User("username", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_ADMIN"));

        val reviews = mock(List.class);

        ReflectionTestUtils.setField(user, "reviews", reviews);

        user.clearReviews(user);

        verify(reviews, times(1)).clear();
    }

    @Test(expected = User.ForbiddenManipulationException.class)
    public void clearReviewsThrowsForbiddenManipulationExceptionIfUserIsNotAdmin() throws Exception {
        user.clearReviews(user);
    }

    @Test(expected = NullPointerException.class)
    public void clearCommentsThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        user.clearComments(null);
    }

    @Test
    public void clearCommentsClearsCommentsFromAdmin() throws Exception {
        val user = new User("username", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_ADMIN"));

        val comments = mock(List.class);

        ReflectionTestUtils.setField(user, "comments", comments);

        user.clearComments(user);

        verify(comments, times(1)).clear();
    }

    @Test(expected = User.ForbiddenManipulationException.class)
    public void clearCommentsThrowsForbiddenManipulationExceptionIfUserIsNotAdmin() throws Exception {
        user.clearComments(user);
    }

    @Test(expected = NullPointerException.class)
    public void setEnabledThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        user.setEnabled(true, null);
    }

    @Test
    public void setEnabledThrowsForbiddenManipulationExceptionIfUserIsNotAdmin() {
        assertThrows(User.ForbiddenManipulationException.class, () ->
                user.setEnabled(true, user));

        assertThrows(User.ForbiddenManipulationException.class, () ->
                user.setEnabled(false, user));
    }

    @Test
    public void setEnabledThrowsForbiddenManipulationExceptionIfUserIsAdminFromAdmin() throws Exception {
        val user = new User("username", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_ADMIN"));

        assertThrows(User.ForbiddenManipulationException.class, () ->
                user.setEnabled(true, user));

        assertThrows(User.ForbiddenManipulationException.class, () ->
                user.setEnabled(false, user));
    }

    @Test
    public void setEnabledChangesEnabledStatusOfNormalUserFromAdmin() throws Exception {
        val adminUser = new User("username", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_ADMIN"));

        user.setEnabled(false, adminUser);

        assertFalse(user.isEnabled());

        user.setEnabled(true, adminUser);

        assertTrue(user.isEnabled());

    }

    @Test
    public void getPublishedGamesCountReturnsPublishedGamesCount() {
        val games = new ArrayList<Game>();

        games.add(unpublishedGame);
        games.add(publishedGame);

        ReflectionTestUtils.setField(user, "games", games);

        assertEquals(1, user.getPublishedGamesCount());
    }

    @Test
    public void getRatedGamesCountReturnsRatedGamesCount() {
        val reviews = new HashSet<Review>();

        val reviewForUnpublishedGame = mock(Review.class);
        when(reviewForUnpublishedGame.getGame()).thenReturn(unpublishedGame);

        val reviewForPublishedGame = mock(Review.class);
        when(reviewForPublishedGame.getGame()).thenReturn(publishedGame);

        reviews.add(reviewForPublishedGame);
        reviews.add(reviewForUnpublishedGame);

        ReflectionTestUtils.setField(user, "reviews", reviews);

        assertEquals(1, user.getRatedGamesCount());
    }


    @Test
    public void getCommentsCountReturnsCommentsCount() {
        val comments = new ArrayList<Comment>();

        val commentForUnpublishedGame = mock(Comment.class);
        when(commentForUnpublishedGame.getGame()).thenReturn(unpublishedGame);

        val commentForPublishedGame = mock(Comment.class);
        when(commentForPublishedGame.getGame()).thenReturn(publishedGame);

        comments.add(commentForPublishedGame);
        comments.add(commentForUnpublishedGame);

        ReflectionTestUtils.setField(user, "comments", comments);

        assertEquals(1, user.getCommentsCount());
    }

    @Test
    public void equalsShouldWorkProperly() throws Exception {
        val game1 = new Game();
        ReflectionTestUtils.setField(game1, "id", 1);
        val game2 = new Game();
        ReflectionTestUtils.setField(game2, "id", 2);

        val gameNode1 = new RoomNode("testRoomNode1", "testRoomName1",
                "testRoomDesc1");
        ReflectionTestUtils.setField(gameNode1, "id", "1");
        val gameNode2 = new RoomNode("testRoomNode2", "testRoomName2",
                "testRoomDesc2");
        ReflectionTestUtils.setField(gameNode2, "id", "2");

        val review1 = new Review();
        ReflectionTestUtils.setField(review1, "id", 1);
        val review2 = new Review();
        ReflectionTestUtils.setField(review2, "id", 2);

        val comment1 = new Comment();
        ReflectionTestUtils.setField(comment1, "id", 1);
        val comment2 = new Comment();
        ReflectionTestUtils.setField(comment2, "id", 2);

        EqualsVerifier.forClass(User.class)
                .usingGetClass()
                .withPrefabValues(Game.class, game1, game2)
                .withPrefabValues(GameNode.class, gameNode1, gameNode2)
                .withPrefabValues(Review.class, review1, review2)
                .withPrefabValues(Comment.class, comment1, comment2)
                .suppress(Warning.SURROGATE_KEY)
                .verify();
    }

    private User makeNormalUser() throws Exception {
        return new User("username", "email@test.com",
                "password", "password",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));
    }
}
