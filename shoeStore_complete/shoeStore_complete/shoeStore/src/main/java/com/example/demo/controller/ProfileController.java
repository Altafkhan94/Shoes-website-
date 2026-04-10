package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    private User getLoggedInUser(Authentication auth, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null) return sessionUser;
        return userService.findByUsername(auth.getName()).orElse(null);
    }

    // ── View Profile ──────────────────────────────
    @GetMapping
    public String profile(Authentication auth, HttpSession session, Model model) {
        User user = getLoggedInUser(auth, session);
        if (user == null) return "redirect:/login";
        session.setAttribute("user", user);
        model.addAttribute("recentOrders", Collections.emptyList()); // No order module yet
        return "profile/edit";
    }

    // ── Edit Profile GET ──────────────────────────
    @GetMapping("/edit")
    public String editPage(Authentication auth, HttpSession session) {
        User user = getLoggedInUser(auth, session);
        if (user == null) return "redirect:/login";
        session.setAttribute("user", user);
        return "profile/edit";
    }

    // ── Edit Profile POST ─────────────────────────
    @PostMapping("/edit")
    public String editProfile(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String username,
                              @RequestParam String email,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String address,
                              @RequestParam(required = false) Integer preferredSize,
                              Authentication auth, HttpSession session, Model model) {

        User user = getLoggedInUser(auth, session);
        if (user == null) return "redirect:/login";

        try {
            user = userService.updateProfile(user, firstName, lastName, email, phone);
            if (address != null) user.setAddress(address);
            if (preferredSize != null) user.setPreferredSize(preferredSize);
            userService.saveUser(user);

            session.setAttribute("user", user);
            model.addAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Could not update profile: " + e.getMessage());
        }

        model.addAttribute("recentOrders", Collections.emptyList());
        return "profile/edit";
    }

    // ── Change Password GET ───────────────────────
    @GetMapping("/change-password")
    public String changePasswordPage(Authentication auth, HttpSession session) {
        User user = getLoggedInUser(auth, session);
        if (user == null) return "redirect:/login";
        session.setAttribute("user", user);
        return "profile/change-password";
    }

    // ── Change Password POST ──────────────────────
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication auth, HttpSession session, Model model) {

        User user = getLoggedInUser(auth, session);
        if (user == null) return "redirect:/login";
        session.setAttribute("user", user);

        if (!userService.checkPassword(user, currentPassword)) {
            model.addAttribute("error", "Current password is incorrect.");
            return "profile/change-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match.");
            return "profile/change-password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "profile/change-password";
        }

        userService.changePassword(user, newPassword);
        model.addAttribute("success", "Password changed successfully!");
        return "profile/change-password";
    }
}
