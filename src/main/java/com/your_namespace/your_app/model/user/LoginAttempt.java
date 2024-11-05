package com.your_namespace.your_app.model.user;

import lombok.Getter;
import lombok.Setter;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.Date;

import com.your_namespace.your_app.model.user.enumeration.LoginFailureReason;
import com.your_namespace.your_app.model.user.enumeration.LoginOutcome;

@Entity
@Getter @Setter
public class LoginAttempt
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private AppUser user;

    @Enumerated(EnumType.STRING)
    private LoginOutcome outcome;

    @Nullable
    @Enumerated(EnumType.STRING)
    private LoginFailureReason failureReason;

    private Date timestamp;

    public static LoginAttempt makeSuccessfulAttempt(AppUser user)
    {
        LoginAttempt successfulAttempt = new LoginAttempt();
        successfulAttempt.setUser(user);
        successfulAttempt.setOutcome(LoginOutcome.SUCCESS);
        successfulAttempt.setTimestamp(new Date());
        return successfulAttempt;
    }

    public static LoginAttempt makeFailedAttempt(AppUser user, LoginFailureReason failureReason)
    {
        LoginAttempt failedAttempt = new LoginAttempt();
        failedAttempt.setUser(user);
        failedAttempt.setOutcome(LoginOutcome.FAILURE);
        failedAttempt.setFailureReason(failureReason);
        failedAttempt.setTimestamp(new Date());
        return failedAttempt;
    }
}