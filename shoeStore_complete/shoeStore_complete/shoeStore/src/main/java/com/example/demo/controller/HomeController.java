package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    // ── Inject logged-in user into session for Thymeleaf ──────────
    private void loadSessionUser(Authentication auth, HttpSession session) {
        if (auth != null && auth.isAuthenticated() && session.getAttribute("user") == null) {
            userService.findByUsername(auth.getName())
                    .ifPresent(u -> session.setAttribute("user", u));
        }
    }

    // ── Home / Dashboard ──────────────────────────
    @GetMapping("/")
    public String home(Model model, Authentication auth, HttpSession session) {
        loadSessionUser(auth, session);

        List<Product> featured = productService.getAllActiveProducts()
                .stream().limit(6).collect(Collectors.toList());
        model.addAttribute("featuredProducts", featured);

        return "dashboard";
    }

    // ── Products Page ─────────────────────────────
    @GetMapping("/products")
    public String products(@RequestParam(required = false) String category,
                           Model model, Authentication auth, HttpSession session) {
        loadSessionUser(auth, session);

        List<Product> products;
        if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllActiveProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("selectedCategory", category);
        return "pages/products";
    }

    // ── About Page ────────────────────────────────
    @GetMapping("/about")
    public String about(Authentication auth, HttpSession session) {
        loadSessionUser(auth, session);
        return "pages/about";
    }

      
 // ── Contact GET ──────────────────────────────
    @GetMapping("/contact")
    public String contact(Authentication auth, HttpSession session, Model model) {
        loadSessionUser(auth, session);
        model.addAttribute("success", false);
        return "pages/contact";
    }

    // ── Contact POST ─────────────────────────────
    @PostMapping("/contact")
    public String contactSubmit(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                @RequestParam(required = false) String phone,
                                @RequestParam String subject,
                                @RequestParam String message,
                                Authentication auth, HttpSession session, Model model) {
        loadSessionUser(auth, session);
        if (message == null || message.trim().length() < 5) {
            model.addAttribute("error", "Please enter a valid message.");
            model.addAttribute("success", false);
            return "pages/contact";
        }
        System.out.println("📩 Contact: " + firstName + " " + lastName + " | " + email + " | " + subject);
        model.addAttribute("success", true);
        return "pages/contact";
    }


  
}