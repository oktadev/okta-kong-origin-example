package com.okta.examples.originexample.controller;

import com.okta.examples.originexample.model.User;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@Scope("request")
public class SecureController {

    private User user;

    public SecureController(User user) {
        this.user = user;
    }

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("user", user);
        return "authenticated";
    }

    @RequestMapping("/users")
    @PreAuthorize("hasAuthority('users')")
    public String users(Model model) {
        model.addAttribute("user", user);
        return "roles";
    }

    @RequestMapping("/admins")
    @PreAuthorize("hasAuthority('admins')")
    public String admins(Model model) {
        model.addAttribute("user", user);
        return "roles";
    }

    @RequestMapping("/403")
    public String error403() {
        return "403";
    }
}