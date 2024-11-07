package com.your_namespace.your_app.service.user;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nonnull;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.your_namespace.your_app.model.user.AppUser;
import com.your_namespace.your_app.model.user.AppUserDto;
import com.your_namespace.your_app.model.user.LoginAttempt;
import com.your_namespace.your_app.model.user.enumeration.AppAuthority;
import com.your_namespace.your_app.model.user.enumeration.LoginFailureReason;
import com.your_namespace.your_app.persistence.user.UserRepository;
import com.your_namespace.your_app.security.annotation.ClearsCsrfToken;
import com.your_namespace.your_app.security.annotation.RequiresClaimsRefresh;
import com.your_namespace.your_app.service.auth.SecurityContextService;
import com.your_namespace.your_app.util.Constants;
import com.your_namespace.your_app.validation.validator.PasswordValidator;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepo;

    private final SecurityContextService securityContextService;

    @Override
    public List<AppUser> findAllFullyHydrated()
    {
        return userRepo.findAllFullyHydrated();
    }

    @Override
    public Optional<AppUser> findById(long id)
    {
        return userRepo.findById(id);
    }

    @Override
    public Optional<AppUser> findWithAuthoritiesById(long id)
    {
        return userRepo.findWithAuthoritiesById(id);
    }

    @Override
    public Optional<AppUser> findFullyHydratedById(long id)
    {
        return userRepo.findFullyHydratedById(id);
    }

    @Override
    public AppUser create(@Nonnull AppUserDto dto)
    {
        String username = dto.getUsername().trim();

        if (StringUtils.isBlank(username))
        {
            throw new IllegalArgumentException(
                AppUserDto.class.getSimpleName() + " with blank username somehow got through validation");
        }

        if (!PasswordValidator.isValid(dto.getPassword()))
        {
            throw new IllegalArgumentException(AppUserDto.class.getSimpleName() +
                " with invalid password somehow got through validation: [" + dto.getPassword() + "]");
        }

        if (userRepo.existsByUsername(username))
        {
            throw new IllegalArgumentException("A user with the username [" + username + "] already exists");
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword().trim());
        AppUser user = new AppUser(
            username,
            hashedPassword,
            dto.getAuthorizedUntil(),
            dto.isEnabled(),
            dto.isAdmin()
        );

        user = userRepo.save(user);
        LOGGER.info("User [{}] created by [{}]", user.getUsername(), getRequesterUsername());

        return user;
    }

    @Override
    @ClearsCsrfToken
    @RequiresClaimsRefresh
    public void delete(long id)
    {
        AppUser user = findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Cannot delete user with id [" + id + "]; user not found"));

        userRepo.delete(user);
        LOGGER.info("User [{}] deleted by [{}]", user.getUsername(), getRequesterUsername());
    }

    @Override
    @RequiresClaimsRefresh
    public void unlock(long id)
    {
        AppUser user = findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Cannot unlock user with id [" + id + "]; user not found"));

        user.setLockedOn(null);
        user.setFailedPasswordAttempts(0);
        user = userRepo.save(user);

        LOGGER.info("User [{}] unlocked by [{}]", user.getUsername(), getRequesterUsername());
    }

    @Override
    @ClearsCsrfToken
    @RequiresClaimsRefresh
    public void setPassword(long id, String password)
    {
        if (!PasswordValidator.isValid(password))
        {
            throw new IllegalArgumentException("Invalid password somehow got through validation: [" + password + "]");
        }

        AppUser user = findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Cannot set password for user with id [" + id + "]; user not found"));

        user.setPassword(passwordEncoder.encode(password.trim()));
        user.setFailedPasswordAttempts(0);
        user = userRepo.save(user);

        LOGGER.info("[{}] changed password for user [{}]",
            getRequesterUsername(),
            user.getUsername()
        );
    }

    @Override
    @RequiresClaimsRefresh
    public void setAuthorizedUntil(long id, Date date)
    {
        AppUser user = findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Cannot set access date for user with id [" + id + "]; user not found"));

        user.setAuthorizedUntil(date);
        user = userRepo.save(user);

        LOGGER.info("User [{}] is authorized until {}; set by [{}]",
            user.getUsername(),
            user.getAuthorizedUntil(),
            getRequesterUsername()
        );
    }

    @Override
    @RequiresClaimsRefresh
    public void setInfiniteAuthorization(long id)
    {
        AppUser user = findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Cannot set infinite access for user id [" + id + "]; user not found"));

        user.setAuthorizedUntil(null);
        user = userRepo.save(user);

        LOGGER.info("User [{}] is authorized forever; set by [{}]",
            user.getUsername(),
            getRequesterUsername()
        );
    }

    @Override
    @RequiresClaimsRefresh
    public void setEnabled(long id, boolean enabled)
    {
        AppUser user = findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Cannot enable/disable user with id [" + id + "]; user not found"));

        user.setEnabled(enabled);
        user = userRepo.save(user);

        LOGGER.info("User [{}] {} by [{}]",
            user.getUsername(),
            user.isEnabled() ? "enabled" : "disabled",
            getRequesterUsername()
        );
    }

    @Override
    @ClearsCsrfToken
    @RequiresClaimsRefresh
    public void setIsAdmin(long id, boolean isAdmin)
    {
        AppUser user = findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Cannot promote/demote user with id [" + id + "]; user not found"));

        user.setAdmin(isAdmin);
        user = userRepo.save(user);

        LOGGER.info("User [{}] {} by [{}]",
            user.getUsername(),
            user.isAdmin() ? "promoted to admin" : "demoted to standard user",
            getRequesterUsername()
        );
    }

    @Override
    @RequiresClaimsRefresh
    public void toggleAuth(long id, @Nonnull AppAuthority authority)
    {
        AppUser user = findWithAuthoritiesById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Cannot grant/remove authority [" + authority + "] for user with id [" + id + "]; user not found"));

        if (user.hasAuthority(authority))
        {
            user.removeAuthority(authority);
        }
        else
        {
            user.addAuthority(authority);
        }

        user = userRepo.save(user);
        boolean granted = user.hasAuthority(authority);

        LOGGER.info("[{}] {} authority [{}] {} user [{}]",
            getRequesterUsername(),
            granted ? "granted" : "removed",
            authority,
            granted ? "to" : "from",
            user.getUsername()
        );
    }

    @Override
    public UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException
    {
        AppUser user = userRepo.findWithAuthoritiesByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User with username [" + username + "] not found"));
        return user;
    }

    @Override
    public void recordSuccessfulLogin(@Nonnull String username)
    {
        AppUser user = userRepo.findFullyHydratedByUsername(username)
            .orElseThrow(() ->
                new IllegalStateException(
                    "Cannot find user [" + username + "] after they successfully logged in...highly unusual"));

        user.getLoginAttempts().add(LoginAttempt.makeSuccessfulAttempt(user));
        user.setFailedPasswordAttempts(0);
        userRepo.save(user);
    }

    @Override
    public void recordFailedLogin(@Nonnull String username, @Nonnull LoginFailureReason failureReason)
    {
        Optional<AppUser> optUser = userRepo.findFullyHydratedByUsername(username);
        if (optUser.isEmpty())
        {
            LOGGER.info("Failed login for non-existent user [{}]", username);
            return;
        }

        AppUser user = optUser.get();

        if (failureReason == LoginFailureReason.BAD_CREDENTIALS)
        {
            user.incrementFailedPasswordAttempts();
            int remainingAttempts = Constants.FAILED_LOGINS_BEFORE_ACCOUNT_LOCK - user.getFailedPasswordAttempts();
            if (remainingAttempts <= 0)
            {
                if (user.isAccountLocked())
                {
                    LOGGER.debug("Recording failed login attempt for [{}]; this account is already locked", username);
                }
                else
                {
                    user.setLockedOn(new Date());
                    LOGGER.info("Recording failed login attempt for [{}]; this account is now locked", username);
                }
            }
            else
            {
                LOGGER.info("Recording failed login attempt for [{}]; user has {} tries remaining before their " +
                    "account is locked", username, remainingAttempts);
            }
        }

        user.getLoginAttempts().add(LoginAttempt.makeFailedAttempt(user, failureReason));
        userRepo.save(user);
    }

    private String getRequesterUsername()
    {
        return securityContextService.getCurrentUsersNameNonNull();
    }
}