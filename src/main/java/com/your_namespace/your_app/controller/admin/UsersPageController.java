package com.your_namespace.your_app.controller.admin;

import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.your_namespace.your_app.model.user.AppUser;
import com.your_namespace.your_app.service.user.UserService;

@Controller
@RequestMapping("/your_app/users") // TODO - set your app's root path
@AllArgsConstructor
public class UsersPageController
{
    private final UserService userService;

    @GetMapping
    public String getUserManagementPage(Model model)
    {
        List<AppUser> allUsers = userService.findAllFullyHydrated();
        model.addAttribute("userList", allUsers);
        return "admin/user-management";
    }

    @GetMapping("/{id}")
    public String getUserManagementPage(@PathVariable long id, Model model)
    {
        AppUser user = userService.findFullyHydratedById(id).
            orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        model.addAttribute("user", user);
        return "admin/view-user";
    }
}