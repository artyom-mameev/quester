package com.artyommameev.quester.util.test;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

/**
 * A simple utility class used in tests to generate a simple string
 * with a specific size.
 *
 * @author Artyom Mameev
 */
@UtilityClass
public class SimpleStringGenerator {

    /**
     * Generates a simple string with 'a' character of a specific size.
     *
     * @param size the size of the string.
     * @return a simple string with 'a' character of the given size.
     */
    public static String generateSimpleString(int size) {
        char[] array = new char[size];
        Arrays.fill(array, 'a');

        return new String(array);
    }
}
