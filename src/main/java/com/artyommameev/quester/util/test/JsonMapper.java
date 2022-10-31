package com.artyommameev.quester.util.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

/**
 * A simple utility class used in tests to map objects into json.
 *
 * @author Artyom Mameev
 */
@UtilityClass
public class JsonMapper {

    /**
     * Maps object into json.
     * <p>
     * When an exception occurs, throws runtime exception.
     *
     * @param obj the object that should be mapped into json.
     * @return a json string with the mapped object.
     */
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
