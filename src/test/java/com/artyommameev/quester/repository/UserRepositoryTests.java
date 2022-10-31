package com.artyommameev.quester.repository;

import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.Review;
import com.artyommameev.quester.entity.User;
import config.TestJpaConfig;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestJpaConfig.class},
        loader = AnnotationConfigContextLoader.class)
@Transactional
@DirtiesContext
@SuppressWarnings("unchecked")
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Mock
    private OidcUser oidcUser;

    @Test
    public void saves() throws Exception {
        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        val games = (List<Game>) ReflectionTestUtils
                .getField(user, "games");

        val game = new Game("TestName", "TestDesc",
                "TestLang", user, true);

        Objects.requireNonNull(games).add(game);

        val reviews = (Set<Review>) ReflectionTestUtils
                .getField(user, "reviews");

        Objects.requireNonNull(
                reviews).add(new Review(Review.Rating.FOUR, game, user));

        val comments = (List<Comment>) ReflectionTestUtils
                .getField(user, "comments");

        Objects.requireNonNull(
                comments).add(new Comment(game, user, "Text"));

        user = userRepository.save(user);

        user = userRepository.findById(user.getId()).orElseThrow();

        assertEquals("TestUsername", user.getUsername());
        assertEquals("test@test.com", user.getEmail());
        assertNotNull(user.getPassword());
        assertTrue(user.isEnabled());
        assertEquals("ROLE_USER", user.getAuthorities().get(0)
                .getAuthority());
        assertEquals(1, user.getPublishedGamesCount());
        assertEquals(1, user.getCommentsCount());
        assertEquals(1, user.getRatedGamesCount());

        when(oidcUser.getAttribute("name")).thenReturn("TestUsername1");
        when(oidcUser.getAttribute("email")).thenReturn("test1@gmail.com");

        user = User.fromGoogleOidcUser(oidcUser);

        user = userRepository.save(user);

        user = userRepository.findById(user.getId()).orElseThrow();

        assertEquals("TestUsername1", user.getGoogleUsername());
        assertEquals("test1@gmail.com", user.getGoogleEmail());
        assertTrue(user.isEnabled());
        assertEquals("ROLE_USER", user.getAuthorities().get(0)
                .getAuthority());
    }

    @Test
    public void edits() throws Exception {
        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        val games = (List<Game>) ReflectionTestUtils
                .getField(user, "games");

        val game = new Game("TestName", "TestDesc",
                "TestLang", user, true);

        Objects.requireNonNull(games).add(game);

        val reviews = (Set<Review>) ReflectionTestUtils
                .getField(user, "reviews");

        Objects.requireNonNull(
                reviews).add(new Review(Review.Rating.FOUR, game, user));

        val comments = (List<Comment>) ReflectionTestUtils
                .getField(user, "comments");

        Objects.requireNonNull(
                comments).add(new Comment(game, user, "Text"));

        user = userRepository.save(user);

        user = userRepository.findById(user.getId()).orElseThrow();

        val oldPass = user.getPassword();

        val admin = new User("Admin", "admin@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_ADMIN"));

        user.updateUsername("NewUsername");
        user.updateEmail("new@test.com");
        user.updatePassword("NewPass", "NewPass",
                new BCryptPasswordEncoder());
        user.setEnabled(false, admin);
        user.clearComments(admin);
        user.clearGames(admin);
        user.clearReviews(admin);

        user = userRepository.save(user);
        user = userRepository.findById(user.getId()).orElseThrow();

        assertEquals("NewUsername", user.getUsername());
        assertEquals("new@test.com", user.getEmail());
        assertNotEquals(oldPass, user.getPassword());
        assertFalse(user.isEnabled());
        assertEquals(0, user.getPublishedGamesCount());
        assertEquals(0, user.getCommentsCount());
        assertEquals(0, user.getRatedGamesCount());

        when(oidcUser.getAttribute("name")).thenReturn("TestUsername1");
        when(oidcUser.getAttribute("email")).thenReturn("test1@gmail.com");

        user = User.fromGoogleOidcUser(oidcUser);
        user = userRepository.save(user);
        user = userRepository.findById(user.getId()).orElseThrow();

        user.confirmUsername("NewUsername");

        assertEquals("NewUsername", user.getUsername());
    }

    @Test
    public void deletes() throws Exception {
        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        userRepository.delete(user);

        assertFalse(userRepository.existsById(user.getId()));

        user = userRepository.save(user);

        userRepository.deleteById(user.getId());

        assertFalse(userRepository.existsById(user.getId()));
    }

    @Test
    public void findsUserByEmail() throws Exception {
        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        val foundUser = userRepository
                .findUserByEmail("test@test.com").orElseThrow();

        Assert.assertEquals(user, foundUser);
    }

    @Test
    public void findsUserByUsername() throws Exception {
        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        val foundUser = userRepository
                .findUserByUsername("TestUsername").orElseThrow();

        Assert.assertEquals(user, foundUser);
    }

    @Test
    public void findsUserByGoogleEmail() {
        when(oidcUser.getAttribute("name")).thenReturn("TestUsername");
        when(oidcUser.getAttribute("email")).thenReturn("test@gmail.com");

        var user = User.fromGoogleOidcUser(oidcUser);

        user = userRepository.save(user);

        val foundUser = userRepository.findUserByGoogleEmail(
                "test@gmail.com").orElseThrow();

        Assert.assertEquals(user, foundUser);
    }
}
