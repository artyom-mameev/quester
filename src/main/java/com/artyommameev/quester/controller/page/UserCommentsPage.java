package com.artyommameev.quester.controller.page;

import com.artyommameev.quester.aspect.CurrentUserToModelAspect;
import com.artyommameev.quester.aspect.annotation.CurrentUserToModel;
import com.artyommameev.quester.controller.page.exception.Page_BadRequestException;
import com.artyommameev.quester.entity.Comment;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.service.CommentService;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * A controller for handling a page with {@link Comment}s from a certain
 * {@link User}.
 *
 * @author Artyom Mameev
 */
@Controller
public class UserCommentsPage {

    private final CommentService commentService;

    private final int pageSize;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param commentService a service that allows to query {@link Comment}
     *                       objects.
     * @param pageSize       the number of comments per page.
     * @see CommentService
     */
    public UserCommentsPage(CommentService commentService,
                            @Value("${quester.page-size}") int pageSize) {
        this.commentService = commentService;
        this.pageSize = pageSize;
    }

    /**
     * Handles GET requests to endpoint '/comments' and returns a page with
     * {@link Comment}s from a certain {@link User}.
     * <p>
     * Adds to the Spring MVC model:
     * <p>
     * 'currentPage' - a number of the current page. Always equals to
     * the 'page' request parameter;<br>
     * 'totalPages' - a number of pages into which the {@link Comment}s
     * are divided;<br>
     * 'comments' - the {@link Comment}s list that represents a portion of the
     * certain {@link User} comments, selected according to the 'page' request
     * parameter;<br>
     * 'user' - the current {@link User} object, adds via
     * {@link CurrentUserToModelAspect}.
     * <p>
     * The page size is equal to the {@link UserCommentsPage#pageSize}.
     *
     * @param model       the Spring MVC model.
     * @param currentPage a request parameter which represents
     *                    the current page number, cannot be missed.
     * @param userId      a path variable that represents an id of
     *                    the {@link User} whose {@link Comment}s should be
     *                    shown, cannot be missed.
     * @return a page with 'user-comments' template.
     * @throws Page_BadRequestException if the page value < 0.
     */
    @RequestMapping("/comments")
    @CurrentUserToModel
    public String showUserCommentsPage(Model model,
                                       @RequestParam(name = "page")
                                       int currentPage,
                                       @RequestParam(name = "user")
                                       int userId) {
        try {
            val commentsPage =
                    commentService.getUserCommentsPage(userId, pageSize,
                            currentPage - 1);

            if (commentsPage.hasContent()) {
                model.addAttribute("comments", commentsPage.getContent());
            }

            int totalPages = commentsPage.getTotalPages() != 0 ?
                    commentsPage.getTotalPages() : 1;

            model.addAttribute("currentPage", currentPage);
            model.addAttribute("totalPages", totalPages);

            return "user-comments";
        } catch (CommentService.IllegalPageValueException e) {
            throw new Page_BadRequestException(e);
        }
    }
}
