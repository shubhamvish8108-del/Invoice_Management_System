package com.codeb.invoice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home Controller - Main entry point
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/invoices";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/invoices";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
