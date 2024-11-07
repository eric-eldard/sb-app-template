package com.your_namespace.your_app.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.net.URI;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.your_namespace.your_app.model.user.AppUser;
import com.your_namespace.your_app.model.user.AppUserDto;
import com.your_namespace.your_app.test.BaseMvcIntegrationTest;
import com.your_namespace.your_app.test.TestUtils;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UsersRestControllerIT extends BaseMvcIntegrationTest
{
    @Inject
    private PasswordEncoder passwordEncoder;


    @Test
    @SneakyThrows
    public void testAdminCanCreateUser()
    {
        AppUserDto newUserDto = makeNonAdminUserDto();

        MvcResult result = post(makeUsersUri(), newUserDto, asAdmin())
            .andExpect(status().isCreated())
            .andReturn();

        long userId = Long.parseLong(result.getResponse().getContentAsString());
        Optional<AppUser> createdUser = userService().findById(userId);

        assertTrue(createdUser.isPresent());
        assertEquals(newUserDto.getUsername(),                       createdUser.get().getUsername());
        assertEquals(newUserDto.isEnabled(),                         createdUser.get().isEnabled());
        assertEquals(newUserDto.isAdmin(),                           createdUser.get().isAdmin());
        assertEquals(newUserDto.getAuthorizedUntil(),                createdUser.get().getAuthorizedUntil());
        assertTrue(passwordEncoder.matches(newUserDto.getPassword(), createdUser.get().getPassword()));
    }

    @Test
    @SneakyThrows
    public void testAuthenticatedViewerCannotCreateUser()
    {
        post(makeUsersUri(), makeNonAdminUserDto(), asUser())
            .andExpect(status().isForbidden());

        assertEquals(1, userService().findAllFullyHydrated().size()); // no user created after the posting user
    }

    @Test
    @SneakyThrows
    public void testUnauthenticatedCannotCreateUser()
    {
        post(makeUsersUri(), makeNonAdminUserDto(), asUnauthenticated())
            .andExpect(status().isFound())
            .andDo(this::assertRedirectToLogin);

        assertTrue(userService().findAllFullyHydrated().isEmpty());
    }


    @Test
    @SneakyThrows
    public void testAdminCanDeleteUser()
    {
        long userId = makeAndSaveNonAdminUser().getId();
        delete(makeUsersUri(userId), asAdmin())
            .andExpect(status().isOk());

        Optional<AppUser> user = userService().findById(userId);
        assertFalse(user.isPresent());
    }

    @Test
    @SneakyThrows
    public void testViewerCannotDeleteUser()
    {
        long userId = makeAndSaveNonAdminUser().getId();
        delete(makeUsersUri(userId), asUser())
            .andExpect(status().isForbidden());

        Optional<AppUser> user = userService().findById(userId);
        assertTrue(user.isPresent());
    }

    @Test
    @SneakyThrows
    public void testUnauthenticatedCannotDeleteUser()
    {
        long userId = makeAndSaveNonAdminUser().getId();
        delete(makeUsersUri(userId), asUnauthenticated())
            .andExpect(status().isFound())
            .andDo(this::assertRedirectToLogin);

        Optional<AppUser> user = userService().findById(userId);
        assertTrue(user.isPresent());
    }


    @Test
    @SneakyThrows
    public void testAdminCanPromoteUser()
    {
        long userId = makeAndSaveNonAdminUser().getId();
        patch(
            makeUsersUri(userId, "admin"),
            AppUserDto.builder().admin(true).build(),
            asAdmin()
        ).andExpect(status().isOk());

        AppUser user = userService().findById(userId).get();
        assertTrue(user.isAdmin());
    }

    @Test
    @SneakyThrows
    public void testViewerCannotPromoteUser()
    {
        long userId = makeAndSaveNonAdminUser().getId();
        patch(
            makeUsersUri(userId, "admin"),
            AppUserDto.builder().admin(true).build(),
            asUser()
        ).andExpect(status().isForbidden());

        AppUser user = userService().findById(userId).get();
        assertFalse(user.isAdmin());
    }

    @Test
    @SneakyThrows
    public void testUnauthenticatedCannotPromoteUser()
    {
        long userId = makeAndSaveNonAdminUser().getId();
        patch(
            makeUsersUri(userId, "admin"),
            AppUserDto.builder().admin(true).build(),
            asUnauthenticated()
        )
            .andExpect(status().isFound())
            .andDo(this::assertRedirectToLogin);

        AppUser user = userService().findById(userId).get();
        assertFalse(user.isAdmin());
    }


    @Test
    @SneakyThrows
    public void testAdminCanUpdateUserPassword()
    {
        AppUser user = makeAndSaveNonAdminUser();
        long userId = user.getId();

        patch(
            makeUsersUri(userId, "password"),
            AppUserDto.builder()
                .password(TestUtils.makePassword())
                .build(),
            asAdmin()
        ).andExpect(status().isOk());

        Optional<AppUser> updatedUser = userService().findById(userId);
        assertNotEquals(user.getPassword(), updatedUser.get().getPassword());
    }

    @Test
    @SneakyThrows
    public void testPasswordMustHaveMinChars()
    {
        AppUser user = makeAndSaveNonAdminUser();
        long userId = user.getId();

        patch(
            makeUsersUri(userId, "password"),
            AppUserDto.builder()
                .password(TestUtils.makeShortPassword())
                .build(),
            asAdmin()
        ).andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void testAuthenticatedViewerCannotUpdateUserPassword()
    {
        AppUser user = makeAndSaveNonAdminUser();
        long userId = user.getId();

        patch(
            makeUsersUri(userId, "password"),
            AppUserDto.builder()
                .password(TestUtils.makePassword())
                .build(),
            asUser()
        ).andExpect(status().isForbidden());

        Optional<AppUser> updatedUser = userService().findById(userId);
        assertEquals(user.getPassword(), updatedUser.get().getPassword());
    }

    @Test
    @SneakyThrows
    public void testUnauthenticatedCannotUpdateUserPassword()
    {
        AppUser user = makeAndSaveNonAdminUser();
        long userId = user.getId();

        patch(
            makeUsersUri(userId, "password"),
            AppUserDto.builder()
                .password(TestUtils.makePassword())
                .build(),
            asUnauthenticated()
        )
            .andExpect(status().isFound())
            .andDo(this::assertRedirectToLogin);

        Optional<AppUser> updatedUser = userService().findById(userId);
        assertEquals(user.getPassword(), updatedUser.get().getPassword());
    }


    @Test
    @SneakyThrows
    public void testCsrfTokenRequired()
    {
        mockMvc().perform(MockMvcRequestBuilders.post(makeUsersUri())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonify(makeNonAdminUserDto()))
            .cookie(asAdmin())
        ).andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    public void testValidCsrfTokenRequired()
    {
        mockMvc().perform(MockMvcRequestBuilders.post(makeUsersUri())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonify(makeNonAdminUserDto()))
            .cookie(asAdmin())
            .with(csrf().useInvalidToken().asHeader())
        ).andExpect(status().isForbidden());
    }


    private URI makeUsersUri()
    {
        return makeUsersUri(null, null);
    }

    private URI makeUsersUri(Long userId)
    {
        return makeUsersUri(userId, null);
    }

    private URI makeUsersUri(@Nullable Long userId, @Nullable String operation)
    {
        StringBuilder fullPath = new StringBuilder("/your_app/users"); // TODO - set your app's root path
        if (userId != null)
        {
            fullPath.append('/').append(userId);
        }
        if (operation != null)
        {
            fullPath.append('/').append(operation);
        }

        return makeBaseUri()
            .path(fullPath.toString())
            .build()
            .toUri();
    }
}