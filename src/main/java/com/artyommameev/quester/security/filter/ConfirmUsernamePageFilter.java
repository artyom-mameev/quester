package com.artyommameev.quester.security.filter;

import com.artyommameev.quester.controller.page.ConfirmUsernamePage;
import com.artyommameev.quester.controller.page.HomePage;
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
 * A Spring Security filter used to prevent {@link User}s with confirmed
 * username from viewing the {@link ConfirmUsernamePage}.
 *
 * @author Artyom Mameev
 */
public class ConfirmUsernamePageFilter extends GenericFilterBean {

    private final ActualUser actualUser;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param actualUser the {@link ActualUser} abstraction which represents
     *                   current normal or oAuth2 user.
     * @see ActualUser
     */
    public ConfirmUsernamePageFilter(ActualUser actualUser) {
        this.actualUser = actualUser;
    }

    /**
     * Basic logic of the filter.<br>
     * If a certain {@link User} requests the {@link ConfirmUsernamePage} and
     * their username is already confirmed, if it is a GET request, the
     * url is redirected to the {@link HomePage}, otherwise an error
     * with {@link HttpServletResponse#SC_FORBIDDEN} status is sent.
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

        /*if a user with confirmed username tries to load
        the username confirmation page*/
        if (requestURI.equals("/confirm-username") &&
                !actualUser.hasUnconfirmedUsername()) {

            if (httpServletRequest.getMethod().equals("GET")) {
                // if GET request, send redirect to the home page
                httpServletResponse.sendRedirect("/");
            } else { // if not, send error with FORBIDDEN status
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            }

            return;
        }

        chain.doFilter(request, response);
    }
}
