package com.your_namespace.your_app.security.password;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AppPasswordEncoder extends BCryptPasswordEncoder
{
}