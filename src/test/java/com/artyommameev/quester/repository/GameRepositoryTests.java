package com.artyommameev.quester.repository;

import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.Review;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.entity.gamenode.GameNode;
import config.TestJpaConfig;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestJpaConfig.class},
        loader = AnnotationConfigContextLoader.class)
@Transactional
@DirtiesContext
@SuppressWarnings("unchecked")
public class GameRepositoryTests {
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void saves() throws Exception {
        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        val createdGame = new Game("TestName", "TestDesc",
                "TestLang", user, true);

        createdGame.addNode("TestId", "###", "TestName",
                "TestDesc", GameNode.NodeType.ROOM, null,
                null, user);
        createdGame.addNode("TestId1", "TestId", "TestName1",
                null, GameNode.NodeType.FLAG, null,
                null, user);
        createdGame.addNode("TestId2", "TestId", "TestName2",
                null, GameNode.NodeType.CHOICE, null,
                null, user);
        createdGame.addNode("TestId3", "TestId2", null,
                null, GameNode.NodeType.CONDITION, "TestId1",
                GameNode.Condition.FlagState.ACTIVE, user);
        createdGame.addComment("TestText", user);
        createdGame.addReview(Review.Rating.FIVE, user);
        createdGame.addFavoritedUser(user);

        val savedGameId = gameRepository.save(createdGame).getId();

        val savedGame = gameRepository.findById(savedGameId)
                .orElseThrow();

        assertEquals("TestName", savedGame.getName());
        assertEquals("TestDesc", savedGame.getDescription());
        assertEquals("TestLang", savedGame.getLanguage());
        assertEquals(user, savedGame.getUser());
        assertNotNull(savedGame.getDate());
        assertTrue(savedGame.isPublished());

        assertEquals("TestId", savedGame.getRootNode().getId());
        assertEquals("TestName", savedGame.getRootNode().getName());
        assertEquals("TestDesc", savedGame.getRootNode()
                .getDescription());
        assertEquals(GameNode.NodeType.ROOM, savedGame.getRootNode().getType());

        assertEquals("TestId1", savedGame.getRootNode()
                .getChildren().get(0).getId());
        assertEquals("TestName1", savedGame.getRootNode()
                .getChildren().get(0).getName());
        assertEquals(GameNode.NodeType.FLAG, savedGame.getRootNode()
                .getChildren().get(0).getType());

        assertEquals("TestId2", savedGame.getRootNode()
                .getChildren().get(1).getId());
        assertEquals("TestName2", savedGame.getRootNode()
                .getChildren().get(1).getName());
        assertEquals(GameNode.NodeType.CHOICE, savedGame.getRootNode()
                .getChildren().get(1).getType());

        assertEquals("TestId3", savedGame.getRootNode()
                .getChildren().get(1).getChildren().get(0).getId());
        assertEquals(GameNode.NodeType.CONDITION, savedGame.getRootNode()
                .getChildren().get(1).getChildren().get(0).getType());
        assertEquals("TestId1", savedGame.getRootNode()
                .getChildren().get(1).getChildren().get(0)
                .getCondition().getFlagId());
        assertEquals(GameNode.Condition.FlagState.ACTIVE,
                savedGame.getRootNode().getChildren().get(1).getChildren().get(0)
                        .getCondition().getFlagState());

        assertEquals("TestText", savedGame.getComments().get(0)
                .getText());

        val reviews = (Set<Review>)
                ReflectionTestUtils.getField(savedGame, "reviews");

        assertEquals(5, Objects.requireNonNull(reviews)
                .iterator().next().getRating());

        val favorited = (Set<User>)
                ReflectionTestUtils.getField(savedGame, "favorited");

        val finalUser = user;

        assertTrue(Objects.requireNonNull(favorited).stream()
                .anyMatch(favUser -> favUser.equals(finalUser)));
    }

    @Test
    public void edits() throws Exception {
        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        val createdGame = new Game("TestName", "TestDesc",
                "TestLang", user, false);

        createdGame.addNode("TestId", "###", "TestName",
                "TestDesc", GameNode.NodeType.ROOM, null,
                null, user);
        createdGame.addNode("TestId1", "TestId", "TestName1",
                null, GameNode.NodeType.FLAG, null,
                null, user);
        createdGame.addNode("TestId2", "TestId", "TestName2",
                null, GameNode.NodeType.CHOICE, null,
                null, user);
        createdGame.addNode("TestId3", "TestId2", null,
                null, GameNode.NodeType.CONDITION, "TestId1",
                GameNode.Condition.FlagState.ACTIVE, user);

        val comments = (List<Comment>) ReflectionTestUtils
                .getField(createdGame, "comments");

        Objects.requireNonNull(
                comments).add(new Comment(createdGame, user, "TestText"));

        comments.add(new Comment(createdGame, user, "TestText2"));

        var reviews = (Set<Review>) ReflectionTestUtils
                .getField(createdGame, "reviews");

        Objects.requireNonNull(
                reviews).add(new Review(Review.Rating.FIVE, createdGame, user));

        var favorited = (Set<User>) ReflectionTestUtils
                .getField(createdGame, "favorited");

        Objects.requireNonNull(favorited).add(user);

        val savedGameId = gameRepository.save(createdGame).getId();
        val savedGame = gameRepository.findById(savedGameId).orElseThrow();

        savedGame.updateName("NewName", user);
        savedGame.updateDescription("NewDesc", user);
        savedGame.updateLanguage("NewLang", user);
        savedGame.updatePublished(true, user);
        savedGame.editNode("TestId", "NewName",
                "NewDesc", null, null,
                user);
        savedGame.addNode("TestId4", "TestId", "TestName4",
                null, GameNode.NodeType.FLAG, null,
                null, user);
        savedGame.editNode("TestId3", "NewName2",
                null, "TestId4",
                GameNode.Condition.FlagState.NOT_ACTIVE, user);
        savedGame.deleteNode("TestId1", user);

        savedGame.editComment(savedGame.getComments().get(0).getId(),
                "NewText", user);
        savedGame.deleteComment(savedGame.getComments().get(1).getId(),
                user);
        savedGame.addComment("TestText3", user);

        savedGame.editReview(Review.Rating.FOUR, user);

        var user2 = new User("TestUsername2", "test2@test.com",
                "TestPass2", "TestPass2",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user2 = userRepository.save(user2);

        savedGame.removeFavoritedUser(user);
        savedGame.addFavoritedUser(user2);

        assertEquals("NewName", savedGame.getName());
        assertEquals("NewDesc", savedGame.getDescription());
        assertEquals("NewLang", savedGame.getLanguage());
        assertTrue(savedGame.isPublished());
        assertNotNull(savedGame.getDate());

        assertEquals("NewName", savedGame.getRootNode().getName());
        assertEquals("NewDesc", savedGame.getRootNode()
                .getDescription());

        assertEquals("TestId4", savedGame.getRootNode()
                .getChildren().get(1).getId());
        assertEquals("TestName4", savedGame.getRootNode()
                .getChildren().get(1).getName());
        assertEquals(GameNode.NodeType.FLAG, savedGame.getRootNode()
                .getChildren().get(1).getType());

        assertEquals("TestId4", savedGame.getRootNode()
                .getChildren().get(0).getChildren().get(0)
                .getCondition().getFlagId());
        assertEquals(GameNode.Condition.FlagState.NOT_ACTIVE,
                savedGame.getRootNode().getChildren().get(0).getChildren().get(0)
                        .getCondition().getFlagState());

        assertEquals("NewText", savedGame.getComments().get(0)
                .getText());
        assertEquals("TestText3", savedGame.getComments().get(1)
                .getText());

        reviews = (Set<Review>)
                ReflectionTestUtils.getField(savedGame, "reviews");

        assertEquals(4, Objects.requireNonNull(reviews)
                .iterator().next().getRating());

        favorited = (Set<User>)
                ReflectionTestUtils.getField(savedGame, "favorited");

        val finalUser = user;

        assertFalse(Objects.requireNonNull(favorited).stream()
                .anyMatch(favUser -> favUser.equals(finalUser)));

        val finalUser2 = user2;

        assertTrue(favorited.stream()
                .anyMatch(favUser -> favUser.equals(finalUser2)));
    }


    @Test
    public void deletes() throws Exception {
        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        val createdGame = new Game("TestName", "TestDesc",
                "TestLang", user, true);

        var savedGame = gameRepository.save(createdGame);

        gameRepository.delete(savedGame);

        assertFalse(gameRepository.existsById(savedGame.getId()));

        savedGame = gameRepository.save(createdGame);

        gameRepository.deleteById(savedGame.getId());

        assertFalse(gameRepository.existsById(savedGame.getId()));
    }

    @Test
    public void returnsGamesWherePublishedIsTrueOrderByDate() throws Exception {
        val gamesList = new ArrayList<Game>();

        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        for (int i = 1; i < 11; i++) {
            val createdGame = new Game("TestName" + i,
                    "TestDesc" + i, "TestLang" + i, user,
                    true);
            ReflectionTestUtils.setField(createdGame, "date",
                    createTestDate(i));
            gamesList.add(createdGame);
        }

        Collections.shuffle(gamesList);

        gameRepository.saveAll(gamesList);

        val gamesPage = gameRepository
                .findGamesByPublishedIsTrueOrderByDate(
                        PageRequest.of(0, 20));

        assertEquals(10, gamesPage.getTotalElements());

        val pageContent = gamesPage.getContent();

        for (int i = 0; i < 10; i++) {
            val game = pageContent.get(i);
            if ((i + 1) > pageContent.size()) {
                assertTrue(Objects.requireNonNull(game.getDate()).before(
                        pageContent.get(i + 1).getDate()));
            }
        }
    }

    @Test
    public void returnsGamesWherePublishedIsTrueOrderByDateDesc() throws Exception {
        val gamesList = new ArrayList<Game>();

        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        for (int i = 1; i < 11; i++) {
            val createdGame = new Game("TestName" + i,
                    "TestDesc" + i, "TestLang" + i, user,
                    true);
            ReflectionTestUtils.setField(createdGame, "date",
                    createTestDate(i));
            gamesList.add(createdGame);
        }

        Collections.shuffle(gamesList);

        gameRepository.saveAll(gamesList);

        val gamesPage =
                gameRepository.findGamesByPublishedIsTrueOrderByDate(
                        PageRequest.of(0, 20));

        assertEquals(10, gamesPage.getTotalElements());

        val pageContent = gamesPage.getContent();

        for (int i = 0; i < 10; i++) {
            val game = pageContent.get(i);
            if ((i + 1) > pageContent.size()) {
                assertTrue(Objects.requireNonNull(game.getDate()).after(
                        pageContent.get(i + 1).getDate()));
            }
        }
    }

    @Test
    public void returnsGamesWherePublishedIsTrueOrderByRatingDesc()
            throws Exception {
        val gamesList = new ArrayList<Game>();

        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        for (int i = 1; i < 11; i++) {
            val createdGame = new Game("TestName" + i,
                    "TestDesc" + i, "TestLang" + i, user,
                    true);

            ReflectionTestUtils.setField(createdGame, "rating",
                    (double) new Random().nextInt(5));

            gamesList.add(createdGame);
        }

        Collections.shuffle(gamesList);

        gameRepository.saveAll(gamesList);

        val gamesPage = gameRepository
                .findGamesByPublishedIsTrueOrderByDate(
                        PageRequest.of(0, 20));

        assertEquals(10, gamesPage.getTotalElements());

        val pageContent = gamesPage.getContent();

        for (int i = 0; i < 10; i++) {
            val game = pageContent.get(i);
            if ((i + 1) > pageContent.size()) {
                assertTrue(game.getRating() >
                        pageContent.get(i + 1).getRating());
            }
        }
    }

    @Test
    public void returnsGamesByUserIdWherePublishedIsTrueOrderByDate()
            throws Exception {
        val gamesList = new ArrayList<Game>();

        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        for (int i = 1; i < 11; i++) {
            val createdGame = new Game("TestName" + i,
                    "TestDesc" + i, "TestLang" + i, user,
                    true);

            ReflectionTestUtils.setField(createdGame, "date",
                    createTestDate(i));

            gamesList.add(createdGame);
        }

        Collections.shuffle(gamesList);

        gameRepository.saveAll(gamesList);

        val gamesPage = gameRepository
                .findGamesByUser_IdAndPublishedIsTrueOrderByDate(
                        user.getId(), PageRequest.of(0, 20));

        assertEquals(10, gamesPage.getTotalElements());

        val pageContent = gamesPage.getContent();

        for (int i = 0; i < 10; i++) {
            val game = pageContent.get(i);
            if ((i + 1) > pageContent.size()) {
                assertTrue(Objects.requireNonNull(game.getDate()).before(
                        pageContent.get(i + 1).getDate()));
            }
        }
    }

    @Test
    public void returnsGamesByUserIdWherePublishedIsTrueOrderByDateDesc()
            throws Exception {
        val gamesList = new ArrayList<Game>();

        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        for (int i = 1; i < 11; i++) {
            val createdGame = new Game("TestName" + i,
                    "TestDesc" + i, "TestLang" + i, user,
                    true);

            ReflectionTestUtils.setField(createdGame, "date",
                    createTestDate(i));

            gamesList.add(createdGame);
        }

        Collections.shuffle(gamesList);

        gameRepository.saveAll(gamesList);

        val gamesPage = gameRepository
                .findGamesByUser_IdAndPublishedIsTrueOrderByDateDesc(
                        user.getId(), PageRequest.of(0, 20));

        assertEquals(10, gamesPage.getTotalElements());

        val pageContent = gamesPage.getContent();

        for (int i = 0; i < 10; i++) {
            val game = pageContent.get(i);
            if ((i + 1) > pageContent.size()) {
                assertTrue(Objects.requireNonNull(game.getDate()).after(
                        pageContent.get(i + 1).getDate()));
            }
        }
    }

    @Test
    public void returnsGamesByUserIdWherePublishedIsTrueOrderByRatingDesc()
            throws Exception {
        val gamesList = new ArrayList<Game>();

        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        for (int i = 1; i < 11; i++) {
            val createdGame = new Game("TestName" + i,
                    "TestDesc" + i, "TestLang" + i, user,
                    true);
            ReflectionTestUtils.setField(createdGame, "rating",
                    (double) new Random().nextInt(5));
            gamesList.add(createdGame);
        }

        Collections.shuffle(gamesList);

        gameRepository.saveAll(gamesList);

        val gamesPage = gameRepository
                .findGamesByUser_IdAndPublishedIsTrueOrderByRatingDesc(
                        user.getId(), PageRequest.of(0, 20));

        assertEquals(10, gamesPage.getTotalElements());

        val pageContent = gamesPage.getContent();

        for (int i = 0; i < 10; i++) {
            val game = pageContent.get(i);
            if ((i + 1) > pageContent.size()) {
                assertTrue(game.getRating() >
                        pageContent.get(i + 1).getRating());
            }
        }
    }

    @Test
    public void returnsGamesFavoritedByUserWherePublishedIsTrueOrderByDate()
            throws Exception {
        val gamesList = new ArrayList<Game>();

        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));
        user = userRepository.save(user);

        for (int i = 1; i < 11; i++) {
            val createdGame = new Game("TestName" + i,
                    "TestDesc" + i, "TestLang" + i, user,
                    true);

            createdGame.addFavoritedUser(user);

            ReflectionTestUtils.setField(createdGame, "date",
                    createTestDate(i));

            gamesList.add(createdGame);
        }

        Collections.shuffle(gamesList);

        gameRepository.saveAll(gamesList);

        val gamesPage = gameRepository
                .findAllByFavoritedIsContainingAndPublishedIsTrueOrderByDate(
                        user, PageRequest.of(0, 20));

        assertEquals(10, gamesPage.getTotalElements());

        val pageContent = gamesPage.getContent();

        for (int i = 0; i < 10; i++) {
            val game = pageContent.get(i);
            if ((i + 1) > pageContent.size()) {
                assertTrue(Objects.requireNonNull(game.getDate()).before(
                        pageContent.get(i + 1).getDate()));
            }
        }
    }

    @Test
    public void returnsGamesFavoritedByUserWherePublishedIsTrueOrderByDateDesc()
            throws Exception {
        val gamesList = new ArrayList<Game>();

        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        for (int i = 1; i < 11; i++) {
            val createdGame = new Game("TestName" + i,
                    "TestDesc" + i, "TestLang" + i, user,
                    true);

            createdGame.addFavoritedUser(user);

            ReflectionTestUtils.setField(createdGame, "date",
                    createTestDate(i));

            gamesList.add(createdGame);
        }

        Collections.shuffle(gamesList);

        gameRepository.saveAll(gamesList);

        val gamesPage = gameRepository
                .findAllByFavoritedIsContainingAndPublishedIsTrueOrderByDateDesc(
                        user, PageRequest.of(0, 20));

        assertEquals(10, gamesPage.getTotalElements());

        val pageContent = gamesPage.getContent();

        for (int i = 0; i < 10; i++) {
            val game = pageContent.get(i);
            if ((i + 1) > pageContent.size()) {
                assertTrue(Objects.requireNonNull(game.getDate()).after(
                        pageContent.get(i + 1).getDate()));
            }
        }
    }

    @Test
    public void returnsGamesFavoritedByUserWherePublishedIsTrueOrderByRatingDesc()
            throws Exception {
        val gamesList = new ArrayList<Game>();

        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        for (int i = 1; i < 11; i++) {
            val createdGame = new Game("TestName" + i,
                    "TestDesc" + i, "TestLang" + i, user,
                    true);

            createdGame.addFavoritedUser(user);

            ReflectionTestUtils.setField(createdGame, "rating",
                    (double) new Random().nextInt(5));

            gamesList.add(createdGame);
        }

        Collections.shuffle(gamesList);

        gameRepository.saveAll(gamesList);

        val gamesPage = gameRepository
                .findAllByFavoritedIsContainingAndPublishedIsTrueOrderByRatingDesc(
                        user, PageRequest.of(0, 20));

        assertEquals(10, gamesPage.getTotalElements());

        val pageContent = gamesPage.getContent();

        for (int i = 0; i < 10; i++) {
            val game = pageContent.get(i);
            if ((i + 1) > pageContent.size()) {
                assertTrue(game.getRating() >
                        pageContent.get(i + 1).getRating());
            }
        }
    }

    @Test
    public void returnsNotPublishedGamesByUserWherePublishedIsTrueOrderByDate()
            throws Exception {
        val gamesList = new ArrayList<Game>();

        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        for (int i = 1; i < 11; i++) {
            val createdGame = new Game("TestName" + i,
                    "TestDesc" + i, "TestLang" + i, user,
                    false);

            ReflectionTestUtils.setField(createdGame, "date",
                    createTestDate(i));

            gamesList.add(createdGame);
        }

        Collections.shuffle(gamesList);

        gameRepository.saveAll(gamesList);

        gameRepository.save(new Game("PublishedName",
                "PublishedDesc", "PublishedLang", user,
                true));

        val gamesPage = gameRepository
                .findGamesByUserAndPublishedIsFalseOrderByDate(
                        user, PageRequest.of(0, 20));

        assertEquals(10, gamesPage.getTotalElements());

        val pageContent = gamesPage.getContent();

        for (int i = 0; i < 10; i++) {
            val game = pageContent.get(i);
            if ((i + 1) > pageContent.size()) {
                assertTrue(Objects.requireNonNull(game.getDate()).before(
                        pageContent.get(i + 1).getDate()));
            }
        }
    }

    private Date createTestDate(int dayOfMonth) {
        return Date.from(LocalDate.of(2021, 1, dayOfMonth)
                .atStartOfDay(ZoneId.of("Europe/Paris")).toInstant());
    }
}
