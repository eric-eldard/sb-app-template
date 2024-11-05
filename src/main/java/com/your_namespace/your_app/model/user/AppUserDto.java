package com.your_namespace.your_app.model.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Date;

import com.your_namespace.your_app.util.Constants;

@Builder
@Getter
@Setter
public class AppUserDto
{
    @Pattern(regexp = "^[A-Za-z0-9\\-_+@.]+$")
    private String username;

    @NotBlank
    @Size(min = Constants.MIN_PASSWORD_CHARS)
    private String password;

    private Date authorizedUntil;

    private boolean enabled;

    private boolean admin;
}