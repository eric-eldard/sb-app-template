package com.your_namespace.your_app.persistence.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.your_namespace.your_app.model.user.AppUser;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long>
{
    @Query("FROM AppUser")
    @EntityGraph(attributePaths = {"loginAttempts", "appAuthorities"})
    List<AppUser> findAllFullyHydrated();

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"loginAttempts", "appAuthorities"})
    Optional<AppUser> findFullyHydratedById(long id);

    @EntityGraph(attributePaths = {"appAuthorities"})
    Optional<AppUser> findWithAuthoritiesById(long id);

    @EntityGraph(attributePaths = {"loginAttempts", "appAuthorities"})
    Optional<AppUser> findFullyHydratedByUsername(String username);

    @EntityGraph(attributePaths = {"appAuthorities"})
    Optional<AppUser> findWithAuthoritiesByUsername(String username);
}