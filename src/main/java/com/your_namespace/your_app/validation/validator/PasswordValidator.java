package com.your_namespace.your_app.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.your_namespace.your_app.util.Constants;
import com.your_namespace.your_app.validation.annotation.ValidPassword;


public class PasswordValidator implements ConstraintValidator<ValidPassword, String>
{
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context)
    {
        return isValid(password);
    }

    public static boolean isValid(String password)
    {
        return password != null &&
            password.trim().length() >= Constants.MIN_PASSWORD_CHARS &&
            password.trim().length() <= Constants.MAX_PASSWORD_CHARS;
    }
}