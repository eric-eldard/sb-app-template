package com.your_namespace.your_app.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Sets;
import com.your_namespace.your_app.util.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Date;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.your_namespace.your_app.YourApp;
import com.your_namespace.your_app.model.user.AppUser;
import com.your_namespace.your_app.model.user.AppUserDto;
import com.your_namespace.your_app.model.user.enumeration.AppAuthority;
import com.your_namespace.your_app.persistence.user.UserRepository;
import com.your_namespace.your_app.security.csrf.CsrfTokenRepository;
import com.your_namespace.your_app.service.auth.AuthenticationService;
import com.your_namespace.your_app.test.TestConfig;
import com.your_namespace.your_app.test.TestUtils;

@SpringBootTest(
    classes = {
        TestConfig.class,
        YourApp.class
    }
)
@ActiveProfiles("test")
public class UserServiceTest
{
    @SpyBean
    private AuthenticationService authenticationService;

    @SpyBean
    private CsrfTokenRepository csrfTokenRepo;

    @SpyBean
    private UserRepository userRepo;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserService userService;


    /**
     * Much faster than {@link DirtiesContext}
     */
    @AfterEach
    public void deleteAllUsers()
    {
        userRepo.deleteAll();
    }


    @Test
    public void testCreateCorrectlyTranscribesDto()
    {
        AppUserDto dto = TestUtils.makeUserDto();
        AppUser user = userService.create(dto);

        assertEquals(1, userRepo.count());

        assertNotNull(user.getId());
        assertEquals(dto.getUsername(),        user.getUsername());
        assertEquals(dto.isEnabled(),          user.isEnabled());
        assertEquals(dto.isAdmin(),            user.isAdmin());
        assertEquals(dto.getAuthorizedUntil(), user.getAuthorizedUntil());
        assertTrue(passwordEncoder.matches(dto.getPassword(), user.getPassword()));
    }

    @Test
    public void testCreateForAdminMakesUserAnAdmin()
    {
        AppUserDto dto = TestUtils.makeUserDto();
        dto.setAdmin(true);
        AppUser user = userService.create(dto);
        assertTrue(user.isAdmin());
    }

    @Test
    public void testCreateFailsForBlankUsername()
    {
        AppUserDto userDto = TestUtils.makeUserDto();
        userDto.setUsername("   ");
        TestUtils.assertThrowsAndPrintMessage(
            IllegalArgumentException.class,
            () -> userService.create(userDto)
        );
        assertTrue(userRepo.findAll().isEmpty());
    }

    @Test
    public void testCreateFailsForExistingUsername()
    {
        userService.create(TestUtils.makeUserDto());
        TestUtils.assertThrowsAndPrintMessage(
            IllegalArgumentException.class,
            () -> userService.create(TestUtils.makeUserDto())
        );
        assertEquals(1, userRepo.count());
    }

    @Test
    public void testCreateFailsForNoPassword()
    {
        AppUserDto userDto = TestUtils.makeUserDto();
        userDto.setPassword(null);
        TestUtils.assertThrowsAndPrintMessage(
            IllegalArgumentException.class,
            () -> userService.create(userDto)
        );
        assertTrue(userRepo.findAll().isEmpty());
    }

    @Test
    public void testCreateFailsForShortPassword()
    {
        AppUserDto userDto = TestUtils.makeUserDto();
        userDto.setPassword(TestUtils.makeShortPassword());
        TestUtils.assertThrowsAndPrintMessage(
            IllegalArgumentException.class,
            () -> userService.create(userDto)
        );
        assertTrue(userRepo.findAll().isEmpty());
    }

    @Test
    public void testDeleteUser()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());

        userService.delete(user.getId());

        verify(userRepo).delete(user);
        verify(authenticationService).requireFreshClaimsForUser(user.getId());
        verify(csrfTokenRepo).invalidateForUser(user.getId());

        assertTrue(userRepo.findAll().isEmpty());
    }

    @Test
    @Transactional
    public void testUnlockUser()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        user.setLockedOn(new Date());
        user.setFailedPasswordAttempts(Constants.FAILED_LOGINS_BEFORE_ACCOUNT_LOCK);
        userRepo.save(user);

        Mockito.reset(userRepo);

        userService.unlock(user.getId());

        verify(userRepo).save(user);
        verify(authenticationService).requireFreshClaimsForUser(user.getId());

        user = userRepo.findById(user.getId()).orElseThrow();

        assertNull(user.getLockedOn());
        assertEquals(0, user.getFailedPasswordAttempts());
    }

    @Test
    public void testSetPassword()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        Mockito.reset(userRepo);

        userService.setPassword(user.getId(), "new-password");

        verify(userRepo).save(user);
        verify(authenticationService).requireFreshClaimsForUser(user.getId());
        verify(csrfTokenRepo).invalidateForUser(user.getId());

        user = userRepo.findById(user.getId()).orElseThrow();

        assertTrue(passwordEncoder.matches("new-password", user.getPassword()));
    }

    @Test
    public void testSetPasswordFailsForShortPassword()
    {
        AppUserDto dto = TestUtils.makeUserDto();
        AppUser user = userService.create(dto);
        Long userId = user.getId();

        Mockito.reset(userRepo);

        TestUtils.assertThrowsAndPrintMessage(
            IllegalArgumentException.class,
            () -> userService.setPassword(userId, TestUtils.makeShortPassword())
        );

        verify(userRepo, never()).save(user);
        verify(authenticationService, never()).requireFreshClaimsForUser(userId);

        user = userRepo.findById(userId).orElseThrow();

        assertTrue(passwordEncoder.matches(dto.getPassword(), user.getPassword())); // assert password unchanged
    }

    @Test
    public void testSetAuthorizedUntil()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        Mockito.reset(userRepo);

        Date twoYearsFromNow = new Date(user.getAuthorizedUntil().getTime() * 2);
        userService.setAuthorizedUntil(user.getId(), twoYearsFromNow);

        verify(userRepo).save(user);
        verify(authenticationService).requireFreshClaimsForUser(user.getId());

        user = userRepo.findById(user.getId()).orElseThrow();

        assertEquals(twoYearsFromNow, user.getAuthorizedUntil());
    }

    @Test
    public void testSetInfiniteAuthorization()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        Mockito.reset(userRepo);

        userService.setInfiniteAuthorization(user.getId());

        verify(userRepo).save(user);
        verify(authenticationService).requireFreshClaimsForUser(user.getId());

        user = userRepo.findById(user.getId()).orElseThrow();

        assertNull(user.getAuthorizedUntil());
    }

    @Test
    public void testSetEnabled()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        Mockito.reset(userRepo);

        userService.setEnabled(user.getId(), false);

        verify(userRepo).save(user);

        user = userRepo.findById(user.getId()).orElseThrow();

        assertFalse(user.isEnabled());
    }

    @Test
    public void testSetAdmin()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        Mockito.reset(userRepo);

        userService.setIsAdmin(user.getId(), true);

        verify(userRepo).save(user);
        verify(csrfTokenRepo).invalidateForUser(user.getId());

        user = userRepo.findById(user.getId()).orElseThrow();

        assertTrue(user.isAdmin());
    }

    @Test
    public void testToggleAuthAddsAuth()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        Mockito.reset(userRepo);

        userService.toggleAuth(user.getId(), AppAuthority.AN_AUTHORITY);

        verify(userRepo).save(user);

        user = userRepo.findWithAuthoritiesById(user.getId()).orElseThrow();

        assertTrue(user.getAppAuthorities().contains(AppAuthority.AN_AUTHORITY));
    }

    @Test
    @Transactional
    public void testToggleAuthRemovesAuth()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        user.setAppAuthorities(Sets.newHashSet(AppAuthority.AN_AUTHORITY));
        userRepo.save(user);
        Mockito.reset(userRepo);

        userService.toggleAuth(user.getId(), AppAuthority.AN_AUTHORITY);

        verify(userRepo).save(user);

        user = userRepo.findWithAuthoritiesById(user.getId()).orElseThrow();

        assertTrue(user.getAppAuthorities().isEmpty());
    }
}