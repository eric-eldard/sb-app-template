package com.your_namespace.your_app.controller.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/your_app") // TODO - set your app's root path
public class AppController
{
    @GetMapping
    public String getMain(Model model)
    {
        return "main"; // TODO - set your app's main jsp
    }

    /**
     * Generic endpoint for serving any JSP that has no dynamic content
     */
    @GetMapping("/content/{content}")
    public String getStaticContent(@PathVariable String content)
    {
        return "content/" + content;
    }

    /**
     * Forwards {@code /your_app/logout} to {@code /logout}
     */
    @GetMapping("/logout")
    public String logout()
    {
        return "forward:/logout";
    }
}