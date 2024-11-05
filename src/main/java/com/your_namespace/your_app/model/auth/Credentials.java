package com.your_namespace.your_app.model.auth;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

@Getter @Setter
public class Credentials
{
    @NotNull
    @Length(min = 1)
    private String username;

    @NotNull
    @Length(min = 1)
    private String password;
}