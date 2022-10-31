package com.artyommameev.quester.service;

import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.repository.CommentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommentServiceTests {

    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private Page<Comment> commentPage;

    @Before
    public void setUp() {
        commentService = new CommentService(commentRepository);
    }

    @Test
    public void getUserCommentsPageThrowsIllegalPageValueExceptionIfPageIsLessThanZero() {
        assertThrows(CommentService.IllegalPageValueException.class,
                () -> commentService.getUserCommentsPage(
                        1, 2, -1));

        assertThrows(CommentService.IllegalPageValueException.class,
                () -> commentService.getUserCommentsPage(
                        1, 2, Integer.MIN_VALUE));
    }

    @Test
    public void getUserCommentsPageGetsCommentPageIfPageIsMoreOrEqualToZero() throws CommentService.IllegalPageValueException {
        when(commentRepository
                .findCommentsByUser_IdAndGame_PublishedIsTrueOrderByDateDesc(
                        1L, PageRequest.of(0, 2)))
                .thenReturn(commentPage);

        assertSame(commentPage, commentService.getUserCommentsPage(
                1L, 2, 0));

        when(commentRepository
                .findCommentsByUser_IdAndGame_PublishedIsTrueOrderByDateDesc(
                        1L, PageRequest.of(Integer.MAX_VALUE, 2)))
                .thenReturn(commentPage);

        assertSame(commentPage, commentService.getUserCommentsPage(
                1L, 2, Integer.MAX_VALUE));
    }
}
