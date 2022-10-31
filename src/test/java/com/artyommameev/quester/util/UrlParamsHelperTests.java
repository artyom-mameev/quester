package com.artyommameev.quester.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class UrlParamsHelperTests {

    @Test
    public void hasParameterChecksIfUrlHasParameter() {
        assertTrue(UrlParamsHelper
                .hasParameter("http://test.com?test=true&actual=false",
                        "test"));

        assertTrue(UrlParamsHelper
                .hasParameter("http://test.com?test=true&actual=false",
                        "actual"));

        assertFalse(UrlParamsHelper
                .hasParameter("http://test.com?test=true&actual=false",
                        "missing"));
    }

    @Test(expected = RuntimeException.class)
    public void hasParameterThrowsRuntimeExceptionIfUrlHasWrongSyntax() {
        UrlParamsHelper.hasParameter(" ", "test");
    }

    @Test
    public void addParameterAddsParameterToUrl() {
        assertEquals("http://test.com?test=true",
                UrlParamsHelper.addParameter("http://test.com",
                        "test", "true"));
        assertEquals("http://test.com?test=true&actual=false",
                UrlParamsHelper.addParameter("http://test.com?test=true",
                        "actual", "false"));
    }

    @Test(expected = RuntimeException.class)
    public void addParameterThrowsRuntimeExceptionIfUrlHasWrongSyntax() {
        UrlParamsHelper.addParameter(" ", "test",
                "true");
    }
}
