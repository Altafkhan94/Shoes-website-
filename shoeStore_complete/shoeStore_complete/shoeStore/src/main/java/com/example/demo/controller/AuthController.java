package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // ── Login ─────────────────────────────────────
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("loggedOut", true);
        }
        return "auth/login";
    }

    // ── Signup ────────────────────────────────────
    @GetMapping("/signup")
    public String signupPage() {
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam String username,
                         @RequestParam String email,
                         @RequestParam(required = false) String phone,
                         @RequestParam String password,
                         @RequestParam String confirmPassword,
                         Model model) {

        // Validation
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            addFormData(model, firstName, lastName, username, email, phone);
            return "auth/signup";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            addFormData(model, firstName, lastName, username, email, phone);
            return "auth/signup";
        }

        try {
            userService.registerUser(firstName, lastName, username, email, phone, password);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            addFormData(model, firstName, lastName, username, email, phone);
            return "auth/signup";
        }

        return "redirect:/login?registered=true";
    }

    private void addFormData(Model model, String firstName, String lastName,
                             String username, String email, String phone) {
        // Pass form data back so user doesn't retype everything
        User temp = new User();
        temp.setFirstName(firstName);
        temp.setLastName(lastName);
        temp.setUsername(username);
        temp.setEmail(email);
        temp.setPhone(phone);
        model.addAttribute("formData", temp);
    }

    // ── Forgot Password ───────────────────────────
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 @RequestParam(required = false) String newPassword,
                                 @RequestParam(required = false) String confirmPassword,
                                 Model model) {

        // Step 1: check if email exists
        if (newPassword == null) {
            if (!userService.existsByEmail(email)) {
                model.addAttribute("error", "No account found with that email address.");
                return "auth/forgot-password";
            }
            // Show success (in real app you'd send email with token)
            model.addAttribute("success", true);
            model.addAttribute("email", email);
            return "auth/forgot-password";
        }

        // Step 2: actually reset (simplified — no token in this demo)
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("email", email);
            return "auth/forgot-password";
        }

        userService.resetPassword(email, newPassword);
        return "redirect:/login?reset=true";
    }

    // ── Logout (handled by Spring Security, but clear session) ────
    @GetMapping("/logout-session")
    public String logoutSession(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}
