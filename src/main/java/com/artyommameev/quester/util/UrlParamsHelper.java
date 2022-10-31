package com.artyommameev.quester.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

/**
 * A simple utility class used to work with a URL parameters.
 *
 * @author Artyom Mameev
 */
@UtilityClass
public class UrlParamsHelper {

    /**
     * Checks if a URL has a certain parameter.
     * <p>
     * Throws runtime exception if the URL has a wrong syntax.
     *
     * @param url       the URL string that should be checked.
     * @param parameter the parameter to check the url for.
     * @return true if the URL has the parameter, otherwise false.
     */
    public static boolean hasParameter(String url, String parameter) {
        try {
            val params = new URIBuilder(url).getQueryParams();

            return params.stream().anyMatch(nameValuePair ->
                    nameValuePair.getName().equalsIgnoreCase(parameter));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a given parameter with a given value to a URL.
     * <p>
     * Throws runtime exception if the URL has a wrong syntax.
     *
     * @param url       the URL string to which the given parameter with the
     *                  given value should be added.
     * @param parameter the parameter that should be added to the URL.
     * @param value     the value of the parameter that should be added to
     *                  the URL.
     */
    public static String addParameter(String url, String parameter, String value) {
        try {
            return new URIBuilder(url).addParameter(parameter, value)
                    .build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
