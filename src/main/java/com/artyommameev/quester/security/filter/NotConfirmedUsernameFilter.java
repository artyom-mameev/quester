package com.artyommameev.quester.security.filter;

import com.artyommameev.quester.controller.page.ConfirmUsernamePage;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import lombok.val;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A Spring Security filter used to prevent oAuth2 {@link User}s with not
 * confirmed username from using functionality of the web-service.
 *
 * @author Artyom Mameev
 */
public class NotConfirmedUsernameFilter extends GenericFilterBean {

    private final ActualUser actualUser;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param actualUser the {@link ActualUser} abstraction which represents
     *                   current normal or oAuth2 user.
     */
    public NotConfirmedUsernameFilter(ActualUser actualUser) {
        this.actualUser = actualUser;
    }

    /**
     * Basic logic of the filter.<br>
     * If an authenticated {@link User} with unconfirmed username requests
     * any url that is not the url of the {@link ConfirmUsernamePage} (except
     * for the static resources urls), if it is a GET request and not an API
     * request, the url is redirected to the {@link ConfirmUsernamePage},
     * otherwise an error with {@link HttpServletResponse#SC_FORBIDDEN} status
     * is sent.
     *
     * @param request  the servlet request.
     * @param response the servlet response.
     * @param chain    the filter chain.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        val httpServletRequest = (HttpServletRequest) request;
        var requestURI = httpServletRequest.getRequestURI();
        val httpServletResponse = (HttpServletResponse) response;

        /*if a user with unconfirmed username is logged and requests any
        url that is not a resources url or the confirm username page url*/
        if (actualUser.isLoggedIn() && actualUser.hasUnconfirmedUsername() &&
                !requestURI.startsWith("/webjars") &&
                !requestURI.startsWith("/css") &&
                !requestURI.startsWith("/themes") &&
                !requestURI.startsWith("/js") &&
                !requestURI.equals("/confirm-username")) {
            if (httpServletRequest.getMethod().equals("GET") &&
                    !requestURI.startsWith("/api/")) {
                /*if GET request and not API request, send redirect to
                the confirm username page*/
                httpServletResponse.sendRedirect("/confirm-username");
            } else {
                /*if not GET request, or GET request to API, send error with
                the FORBIDDEN status  */
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "You must confirm your username");
            }

            return;
        }

        chain.doFilter(request, response);
    }
}
