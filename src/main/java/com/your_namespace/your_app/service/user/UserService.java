package com.your_namespace.your_app.service.user;

import jakarta.annotation.Nonnull;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.your_namespace.your_app.model.user.AppUser;
import com.your_namespace.your_app.model.user.AppUserDto;
import com.your_namespace.your_app.model.user.enumeration.LoginFailureReason;
import com.your_namespace.your_app.model.user.enumeration.AppAuthority;

public interface UserService extends UserDetailsService
{
    List<AppUser> findAllFullyHydrated();

    Optional<AppUser> findById(long id);

    Optional<AppUser> findWithAuthoritiesById(long id);

    Optional<AppUser> findFullyHydratedById(long id);

    boolean hasAdmin();

    AppUser create(@Nonnull AppUserDto dto);

    void delete(long id);

    void unlock(long id);

    void setPassword(long id, String password);

    void setAuthorizedUntil(long id, Date date);

    void setInfiniteAuthorization(long id);

    void setEnabled(long id, boolean enabled);

    void setIsAdmin(long id, boolean isAdmin);

    void toggleAuth(long id, @Nonnull AppAuthority authority);

    void recordSuccessfulLogin(@Nonnull String username);

    void recordFailedLogin(@Nonnull String username, @Nonnull LoginFailureReason failureReason);
}