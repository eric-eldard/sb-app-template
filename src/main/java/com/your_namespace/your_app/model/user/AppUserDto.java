package com.your_namespace.your_app.model.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Pattern;
import java.util.Date;

import com.your_namespace.your_app.validation.annotation.ValidPassword;

@Builder
@Getter
@Setter
public class AppUserDto
{
    @Pattern(regexp = "^[A-Za-z0-9\\-_+@.]+$")
    private String username;

    @ValidPassword
    private String password;

    private Date authorizedUntil;

    private boolean enabled;

    private boolean admin;
}