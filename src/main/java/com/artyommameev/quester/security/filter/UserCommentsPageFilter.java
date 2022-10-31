package com.artyommameev.quester.security.filter;

import com.artyommameev.quester.controller.page.UserCommentsPage;
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

import static com.artyommameev.quester.util.UrlParamsHelper.addParameter;
import static com.artyommameev.quester.util.UrlParamsHelper.hasParameter;

/**
 * A Spring Security filter used to automatically add missing parameters to
 * the url of the {@link UserCommentsPage}.
 *
 * @author Artyom Mameev
 */
public class UserCommentsPageFilter extends GenericFilterBean {

    private final ActualUser actualUser;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param actualUser the {@link ActualUser} abstraction which represents
     *                   current normal or oAuth2 user.
     * @see ActualUser
     */
    public UserCommentsPageFilter(ActualUser actualUser) {
        this.actualUser = actualUser;
    }

    /**
     * Basic logic of the filter.<br>
     * If an {@link User} requests the {@link UserCommentsPage} and the url of
     * the page lacks the necessary parameters, they are added to the url and
     * the {@link User} is redirected to that address.
     *
     * @param request  the servlet request.
     * @param response the servlet response.
     * @param chain    the filter chain.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        val httpServletRequest = (HttpServletRequest) request;
        val requestURI = httpServletRequest.getRequestURI();
        val httpServletResponse = (HttpServletResponse) response;

        if (requestURI.matches("/comments")) {
            val queryString = httpServletRequest.getQueryString();
            var requestURIWithQuery = queryString != null ?
                    requestURI + "?" + queryString : requestURI;

            String correctedRequest;

            correctedRequest = addPageParamIfNotExist(requestURIWithQuery);
            correctedRequest = addUserParamIfNotExist(correctedRequest);

            //if request is corrected and this is not the same request
            if (correctedRequest != null && !requestURIWithQuery.equals(
                    correctedRequest)) {
                httpServletResponse.sendRedirect(correctedRequest);

                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String addPageParamIfNotExist(String requestURIWithQuery) {
        if (!hasParameter(requestURIWithQuery, "page")) {
            return addParameter(requestURIWithQuery, "page", "1");
        }

        return requestURIWithQuery;
    }

    private String addUserParamIfNotExist(String requestURIWithQuery) {
        if (!hasParameter(requestURIWithQuery, "user")) {
            return addParameter(requestURIWithQuery, "user",
                    String.valueOf(actualUser.getId()));
        }

        return requestURIWithQuery;
    }
}
