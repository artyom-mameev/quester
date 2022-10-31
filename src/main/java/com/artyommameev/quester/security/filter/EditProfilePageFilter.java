package com.artyommameev.quester.security.filter;

import com.artyommameev.quester.controller.page.EditProfilePage;
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
 * A Spring Security filter used to prevent oAuth2 {@link User}s from viewing
 * the {@link EditProfilePage}.
 *
 * @author Artyom Mameev
 */
public class EditProfilePageFilter extends GenericFilterBean {

    private final ActualUser actualUser;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param actualUser the {@link ActualUser} abstraction which represents
     *                   current normal or oAuth2 user.
     * @see ActualUser
     */
    public EditProfilePageFilter(ActualUser actualUser) {
        this.actualUser = actualUser;
    }

    /**
     * Basic logic of the filter.<br>
     * If an oAuth2 {@link User} requests the {@link EditProfilePage}, an error
     * with {@link HttpServletResponse#SC_NOT_FOUND} status is sent.
     *
     * @param request  the servlet request.
     * @param response the servlet response.
     * @param chain    the filter chain.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        val httpServletRequest = (HttpServletRequest) request;
        val httpServletResponse = (HttpServletResponse) response;
        val requestURI = httpServletRequest.getRequestURI();

        if (requestURI.equals("/profile") && actualUser.isOauth2User()) {
            // oauth2 user cannot access the profile page
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        chain.doFilter(request, response);
    }

}
