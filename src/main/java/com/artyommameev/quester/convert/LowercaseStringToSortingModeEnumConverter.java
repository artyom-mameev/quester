package com.artyommameev.quester.convert;

import com.artyommameev.quester.service.GameService;
import org.springframework.core.convert.converter.Converter;


/**
 * A simple converter that is used to convert lowercase strings
 * from URL parameters to the corresponding {@link GameService.SortingMode}
 * enumeration values.
 *
 * @author Artyom Mameev
 */
public class LowercaseStringToSortingModeEnumConverter
        implements Converter<String, GameService.SortingMode> {

    /**
     * Converts lowercase strings to the corresponding
     * {@link GameService.SortingMode} enumeration values.
     *
     * @param source the lowercase string that should be converted into the
     *               {@link GameService.SortingMode} enumeration value
     * @return a {@link GameService.SortingMode} enumeration value based on
     * the source string.
     * @throws IllegalArgumentException if the string cannot be converted to the
     *                                  {@link GameService.SortingMode}
     *                                  enumeration value.
     */
    @Override
    public GameService.SortingMode convert(String source) {
        return GameService.SortingMode.valueOf(source.toUpperCase());
    }
}
