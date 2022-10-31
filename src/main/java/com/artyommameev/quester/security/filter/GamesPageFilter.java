package com.artyommameev.quester.security.filter;

import com.artyommameev.quester.controller.page.GamesPage;
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

import static com.artyommameev.quester.util.UrlParamsHelper.addParameter;
import static com.artyommameev.quester.util.UrlParamsHelper.hasParameter;

/**
 * A Spring Security filter used to automatically add missing parameters to
 * the urls of the {@link GamesPage}.
 *
 * @author Artyom Mameev
 */
public class GamesPageFilter extends GenericFilterBean {

    /**
     * Basic logic of the filter.<br>
     * If a certain {@link User} requests a {@link GamesPage} and the url of the
     * page lacks the necessary parameters, they are added to the url and the
     * {@link User} is redirected to that address.
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

        val queryString = httpServletRequest.getQueryString();
        val requestURIWithQuery = queryString != null ?
                requestURI + "?" + queryString : requestURI;

        String correctedRequest = null;

        if (requestURI.equals("/games") || requestURI.equals("/favorites")) {
            //add page and sort params
            correctedRequest = addPageParamIfNotExist(requestURIWithQuery);
            correctedRequest = addSortParamIfNotExist(correctedRequest);
        }
        if (requestURI.equals("/in-work")) {
            //add only page param
            correctedRequest = addPageParamIfNotExist(requestURIWithQuery);
        }
        //if request is corrected and it's not the same page
        if (correctedRequest != null && !requestURIWithQuery.equals(
                correctedRequest)) {
            httpServletResponse.sendRedirect(correctedRequest);

            return;
        }

        chain.doFilter(request, response);
    }

    private String addPageParamIfNotExist(String requestURIWithQuery) {
        if (!hasParameter(requestURIWithQuery, "page")) {
            return addParameter(requestURIWithQuery, "page", "1");
        }

        return requestURIWithQuery;
    }

    private String addSortParamIfNotExist(String requestURIWithQuery) {
        if (!hasParameter(requestURIWithQuery, "sort")) {
            return addParameter(requestURIWithQuery, "sort",
                    "newest");
        }

        return requestURIWithQuery;
    }
}
