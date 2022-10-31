package com.artyommameev.quester.repository;

import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.entity.Game;
import com.artyommameev.quester.entity.User;
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

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestJpaConfig.class},
        loader = AnnotationConfigContextLoader.class)
@Transactional
@DirtiesContext
public class CommentRepositoryTests {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    public void returnsCommentsByUserIdWherePublishedIsTrueOrderByDateDesc() throws Exception {
        val commentsList = new ArrayList<Comment>();

        var user = new User("TestUsername", "test@test.com",
                "TestPass", "TestPass",
                new BCryptPasswordEncoder(),
                Collections.singletonList("ROLE_USER"));

        user = userRepository.save(user);

        var game = new Game("TestName", "TestDesc",
                "TestLang", user, true);

        game = gameRepository.save(game);

        for (int i = 1; i < 11; i++) {
            val comment = new Comment(game, user, i + "Comment");
            commentsList.add(comment);
        }

        Collections.shuffle(commentsList);

        ReflectionTestUtils.setField(game, "comments", commentsList);

        gameRepository.save(game);

        val commentsPage = commentRepository
                .findCommentsByUser_IdAndGame_PublishedIsTrueOrderByDateDesc(
                        user.getId(), PageRequest.of(0, 20));

        assertEquals(10, commentsPage.getTotalElements());

        val pageContent = commentsPage.getContent();

        for (int i = 0; i < 10; i++) {
            val comment = pageContent.get(i);
            if ((i + 1) > pageContent.size()) {
                assertTrue(Integer.parseInt(comment.getText()) <
                        Integer.parseInt(pageContent.get(i + 1).getText()));
            }
        }
    }
}
