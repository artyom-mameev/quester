package com.artyommameev.quester.security.filter;

import com.artyommameev.quester.controller.page.HomePage;
import com.artyommameev.quester.controller.page.RegisterPage;
import com.artyommameev.quester.entity.User;
import lombok.val;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.artyommameev.quester.util.AuthenticationChecker.isAuthenticated;

/**
 * A Spring Security filter used to prevent authenticated {@link User}s from
 * viewing the {@link RegisterPage}.
 *
 * @author Artyom Mameev
 */
public class RegisterPageFilter extends GenericFilterBean {

    /**
     * Basic logic of the filter.<br>
     * If an authenticated {@link User} requests the {@link RegisterPage}, if
     * it is a GET request, the url is redirected to the {@link HomePage},
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

        //if an authenticated user requests the register page
        if (requestURI.equals("/register") && isAuthenticated()) {

            if (httpServletRequest.getMethod().equals("GET")) {
                //if GET request, redirect to home page
                httpServletResponse.sendRedirect("/");
            } else {
                //if not, send error with the FORBIDDEN status
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            }

            return;
        }

        chain.doFilter(request, response);
    }
}
