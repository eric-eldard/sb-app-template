package com.your_namespace.your_app.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.webmvc.error.ErrorController
{
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model)
    {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        model.addAttribute("status", status);
        return "error";
    }
}