package com.your_namespace.your_app.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * Calling an annotated method will remove any saved CSRF tokens for the user.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClearsCsrfToken
{
    @AliasFor("idParamName")
    String value() default "id";

    @AliasFor("value")
    String idParamName() default "id";
}
