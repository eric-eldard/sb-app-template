package com.your_namespace.your_app.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * Add this to a method to indicate the method changes a user's account in a way that will require a fresh lookup of
 * their claims the next time they present an auth token. For example, a method which gives a user admin permission
 * should include this annotation so that we can upgrade the user's claims the next time they're presented.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresClaimsRefresh
{
    @AliasFor("idParamName")
    String value() default "id";

    @AliasFor("value")
    String idParamName() default "id";
}