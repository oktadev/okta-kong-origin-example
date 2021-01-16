package com.okta.examples.originexample.controller;

import com.okta.examples.originexample.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SecureController {

    @RequestMapping("/")
    public String home(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("user", user);
        return "authenticated";
    }

    @RequestMapping("/users")
    @PreAuthorize("hasAuthority('users')")
    public String users(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("user", user);
        return "roles";
    }

    @RequestMapping("/admins")
    @PreAuthorize("hasAuthority('admins')")
    public String admins(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("user", user);
        return "roles";
    }

    @RequestMapping("/403")
    public String unauthorized() {
        return "/403";
    }
}