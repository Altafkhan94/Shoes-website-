package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    // ── Helper: session me user load karo ────────
    private User getSessionUser(Authentication auth, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null) return sessionUser;

        if (auth != null && auth.isAuthenticated()) {
            User dbUser = userService.findByUsername(auth.getName()).orElse(null);
            if (dbUser != null) {
                session.setAttribute("user", dbUser);
            }
            return dbUser;
        }
        return null;
    }

    // ── HOME PAGE ─────────────────────────────────
    @GetMapping("/home")
    public String homePage(Authentication auth, HttpSession session, Model model) {
        User user = getSessionUser(auth, session);
        if (user == null) return "redirect:/login";

        List<Product> featured = productService.getAllActiveProducts()
                .stream().limit(6).collect(Collectors.toList());
        model.addAttribute("featuredProducts", featured);

        return "user/home";
    }

    // ── PROFILE PAGE GET ──────────────────────────
    @GetMapping("/profile")
    public String profilePage(Authentication auth, HttpSession session, Model model) {
        User user = getSessionUser(auth, session);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "user/profile";
    }

    // ── PROFILE UPDATE POST ───────────────────────
    @PostMapping("/profile")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                Authentication auth, HttpSession session, Model model) {

        User user = getSessionUser(auth, session);
        if (user == null) return "redirect:/login";

        try {
            user = userService.updateProfile(user, firstName, lastName, email, phone);
            if (address != null) {
                user.setAddress(address);
                userService.saveUser(user);
            }
            session.setAttribute("user", user);
            model.addAttribute("user", user);
            model.addAttribute("success", "Profile successfully updated!");
        } catch (Exception e) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Update failed: " + e.getMessage());
        }

        return "user/profile";
    }

    // ── CART PAGE ─────────────────────────────────
    @GetMapping("/cart")
    public String cartPage(Authentication auth, HttpSession session, Model model) {
        User user = getSessionUser(auth, session);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "user/cart";
    }
}