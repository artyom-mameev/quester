package com.artyommameev.quester.security.test;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation used with {@link WithMockCustomUserSecurityContextFactory}.
 *
 * @author Artyom Mameev
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    String username() default "testName";

    boolean admin() default false;
}
