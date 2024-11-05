package com.your_namespace.your_app.model.user;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.your_namespace.your_app.model.user.enumeration.AppAuthority;
import com.your_namespace.your_app.model.user.enumeration.LoginOutcome;

@Entity
@Getter @Setter
public class AppUser implements UserDetails
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @OneToMany(
        cascade = CascadeType.ALL,
        mappedBy = "user",
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private List<LoginAttempt> loginAttempts;

    private int failedPasswordAttempts;

    private Date lockedOn;

    private Date authorizedUntil;

    private boolean enabled;

    private boolean admin;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = AppAuthority.class)
    @Column(name = "authority")
    @JoinTable(name = "grant_authority", joinColumns = @JoinColumn(name = "user_id"))
    private Set<AppAuthority> appAuthorities;

    public AppUser()
    {
        // framework ctor
    }

    public AppUser(String username, String password, Date authorizedUntil, boolean enabled, boolean admin)
    {
        this.username = username;
        this.password = password;
        this.authorizedUntil = authorizedUntil;
        this.enabled = enabled;
        this.admin = admin;
    }

    @Transient
    public boolean isAccountLocked()
    {
        return lockedOn != null;
    }

    @Transient
    public Date getLastSuccessfulLoginTimestamp()
    {
        return loginAttempts.stream()
            .filter(attempt -> attempt.getOutcome() == LoginOutcome.SUCCESS)
            .map(LoginAttempt::getTimestamp)
            .max(Date::compareTo)
            .orElse(null);
    }

    @Transient
    public boolean addAuthority(AppAuthority authority)
    {
        ensureAuthorityCollection();
        return appAuthorities.add(authority);
    }

    @Transient
    public boolean removeAuthority(AppAuthority authority)
    {
        ensureAuthorityCollection();
        return appAuthorities.remove(authority);
    }

    @Transient
    public boolean hasAuthority(AppAuthority authority)
    {
        return appAuthorities != null && appAuthorities.contains(authority);
    }

    private void ensureAuthorityCollection()
    {
        if (appAuthorities == null)
        {
            appAuthorities = new HashSet<>();
        }
    }

    @Transient
    public void incrementFailedPasswordAttempts()
    {
        failedPasswordAttempts++;
    }

    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        Set<SimpleGrantedAuthority> grantAuthorities = new HashSet<>();
        if (admin)
        {
            grantAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (appAuthorities != null)
        {
            appAuthorities.forEach(auth ->
                grantAuthorities.add(new SimpleGrantedAuthority(auth.toString()))
            );
        }
        return Set.copyOf(grantAuthorities);
    }

    @Override
    @Transient
    public boolean isAccountNonExpired()
    {
        return authorizedUntil == null || authorizedUntil.after(new Date());
    }

    @Override
    @Transient
    public boolean isAccountNonLocked()
    {
        return !isAccountLocked();
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    /**
     * @return {@link #loginAttempts} sorted in descending {@link LoginAttempt#timestamp} order
     */
    @Transient
    public List<LoginAttempt> getLoginAttemptsSorted()
    {
        return loginAttempts.stream()
            .sorted(Comparator.comparing(LoginAttempt::getTimestamp).reversed())
            .toList();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof AppUser other && id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode()
    {
        if (id == null)
        {
            throw new UnsupportedOperationException("Can't call hashCode() on transient " + getClass().getSimpleName());
        }
        return Math.toIntExact(id);
    }
}