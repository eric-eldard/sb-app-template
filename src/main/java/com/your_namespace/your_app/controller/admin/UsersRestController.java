package com.your_namespace.your_app.controller.admin;

import lombok.AllArgsConstructor;

import jakarta.validation.Valid;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.your_namespace.your_app.model.user.AppUserDto;
import com.your_namespace.your_app.model.user.enumeration.AppAuthority;
import com.your_namespace.your_app.service.user.UserService;

@RestController
@RequestMapping("/your_app/users") // TODO - set your app's root path
@AllArgsConstructor
public class UsersRestController
{
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public long create(@RequestBody @Valid AppUserDto dto)
    {
        return userService.create(dto).getId();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id)
    {
        userService.delete(id);
    }

    @PatchMapping("/{id}/unlock")
    public void unlock(@PathVariable long id)
    {
        userService.unlock(id);
    }

    @PatchMapping("/{id}/password")
    public void setPassword(@PathVariable long id, @RequestBody @Valid AppUserDto dto)
    {
        userService.setPassword(id, dto.getPassword());
    }

    @PatchMapping("/{id}/authorized-until")
    public void setAuthorizedUntil(@PathVariable long id, @RequestBody AppUserDto dto)
    {
        Date until = dto.getAuthorizedUntil();
        if (until == null)
        {
            userService.setInfiniteAuthorization(id);
        }
        else
        {
            userService.setAuthorizedUntil(id, until);
        }
    }

    @PatchMapping("/{id}/enabled")
    public void setEnabled(@PathVariable long id, @RequestBody AppUserDto dto)
    {
        userService.setEnabled(id, dto.isEnabled());
    }

    @PatchMapping("/{id}/admin")
    public void setIsAdmin(@PathVariable long id, @RequestBody AppUserDto dto)
    {
        userService.setIsAdmin(id, dto.isAdmin());
    }

    @PatchMapping("/{id}/toggle-auth/{authority}")
    public void toggleAuth(@PathVariable long id, @PathVariable AppAuthority authority)
    {
        userService.toggleAuth(id, authority);
    }
}