package com.artyommameev.quester.convert;

import com.artyommameev.quester.service.GameService;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LowercaseStringToSortingModeEnumConverterTests {

    @Test
    public void converts() {
        val stringToEnumConverter =
                new LowercaseStringToSortingModeEnumConverter();

        Assert.assertEquals(GameService.SortingMode.OLDEST,
                stringToEnumConverter.convert("oldest"));
        Assert.assertEquals(GameService.SortingMode.NEWEST,
                stringToEnumConverter.convert("newest"));
        Assert.assertEquals(GameService.SortingMode.RATING,
                stringToEnumConverter.convert("rating"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgumentExceptionIfTryingToConvertStringThatHasNoCorrespondingSortingModeEnumValue() {
        val stringToEnumConverter =
                new LowercaseStringToSortingModeEnumConverter();

        stringToEnumConverter.convert("illegal");
    }
}
