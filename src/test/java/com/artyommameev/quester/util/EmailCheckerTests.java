package com.artyommameev.quester.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RunWith(JUnit4.class)
public class EmailCheckerTests {

    @Test
    public void returnsTrueIfEmailIsCorrect() {
        assertTrue(EmailChecker.isEmail("test@email.com"));
        assertTrue(EmailChecker.isEmail("email@test.org"));
    }

    @Test
    public void returnsFalseIfEmailIsNotCorrect() {
        assertFalse(EmailChecker.isEmail("test.email.com"));
        assertFalse(EmailChecker.isEmail("@email.com"));
    }
}
