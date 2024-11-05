package com.your_namespace.your_app.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.your_namespace.your_app.model.user.AppUserDto;
import com.your_namespace.your_app.util.Constants;

public class TestUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

    public static <T extends Throwable> T assertThrowsAndPrintMessage(Class<T> expectedType, Executable executable)
    {
        T throwable = assertThrows(expectedType, executable);
        LOGGER.error("{} thrown with message: {}", expectedType.getSimpleName(), throwable.getMessage());
        return throwable;
    }

    public static AppUserDto makeUserDto()
    {
        Date nextYear = new Date(System.currentTimeMillis() + Duration.ofDays(365).toMillis());
        return AppUserDto.builder()
            .username("standard-user")
            .password(makePassword())
            .enabled(true)
            .authorizedUntil(nextYear)
            .build();
    }

    public static String makePassword()
    {
        return "x".repeat(Constants.MIN_PASSWORD_CHARS);
    }

    public static String makeShortPassword()
    {
        return "x".repeat(Constants.MIN_PASSWORD_CHARS - 1);
    }

    public static Date twoDaysAgo()
    {
        return dateAtSystemTz(LocalDateTime.now().minusDays(2));
    }

    public static Date yesterday()
    {
        return dateAtSystemTz(LocalDateTime.now().minusDays(1));
    }

    public static Date tomorrow()
    {
        return dateAtSystemTz(LocalDateTime.now().plusDays(1));
    }

    private static Date dateAtSystemTz(LocalDateTime dateTime)
    {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}