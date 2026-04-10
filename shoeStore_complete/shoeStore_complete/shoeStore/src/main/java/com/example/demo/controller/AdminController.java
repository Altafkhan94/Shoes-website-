package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    // ── Load admin into session ───────────────────
    private void loadAdminSession(Authentication auth, HttpSession session) {
        if (auth != null && session.getAttribute("admin") == null) {
            userService.findByUsername(auth.getName())
                    .ifPresent(u -> session.setAttribute("admin", u));
        }
    }

    // ── Admin Dashboard ───────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, HttpSession session, Model model) {
        loadAdminSession(auth, session);

        model.addAttribute("totalProducts", productService.countAllProducts());
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalOrders", 0L);       // No order module yet
        model.addAttribute("totalRevenue", 0.0);     // No order module yet
        model.addAttribute("recentOrders", Collections.emptyList());
        model.addAttribute("topProducts", productService.getAllActiveProducts()
                .stream().limit(5).collect(Collectors.toList()));

        return "admin/dashboard";
    }

    // ── Products List ─────────────────────────────
    @GetMapping("/products")
    public String products(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(required = false) String category,
                           Authentication auth, HttpSession session, Model model) {
        loadAdminSession(auth, session);

        Page<Product> productPage = productService.getProductsPage(page, size, category);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("selectedCategory", category);

        return "admin/products";
    }

    // ── Product Form GET (Add/Edit) ──────────────
    @GetMapping("/product-form")
    public String productFormEdit(@RequestParam(required = false) Long id,
                                  Authentication auth, HttpSession session, Model model) {
        loadAdminSession(auth, session);
        if (id != null) {
            Product product = productService.getProductById(id)
                    .orElse(new Product());
            model.addAttribute("product", product);
        } else {
            model.addAttribute("product", new Product());
        }
        return "admin/product-form";
    }

    // ── Add Product ───────────────────────────────
    @PostMapping("/products/add")
    public String addProduct(@RequestParam String name,
                             @RequestParam String brand,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) String gender,
                             @RequestParam(required = false) String description,
                             @RequestParam Double price,
                             @RequestParam(required = false) Double originalPrice,
                             @RequestParam(required = false) String imageUrl,
                             @RequestParam(required = false) String sizes,
                             @RequestParam(required = false) String color,
                             @RequestParam(required = false) Integer stock,
                             @RequestParam(required = false) Boolean active,
                             @RequestParam(required = false) Boolean isNew,
                             RedirectAttributes ra) {

        Product p = new Product();
        p.setName(name);
        p.setBrand(brand);
        p.setCategory(category);
        p.setGender(gender);
        p.setDescription(description);
        p.setPrice(price);
        p.setOriginalPrice(originalPrice);
        p.setImageUrl(imageUrl);
        p.setSizes(sizes);
        p.setColor(color);
        p.setStock(stock != null ? stock : 0);
        p.setActive(active != null && active);
        p.setIsNew(isNew != null && isNew);

        productService.saveProduct(p);
        ra.addFlashAttribute("success", "Product \"" + name + "\" added successfully!");
        return "redirect:/admin/products";
    }

    // ── Edit Product ──────────────────────────────
    @PostMapping("/products/edit")
    public String editProduct(@RequestParam Long id,
                              @RequestParam String name,
                              @RequestParam String brand,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) String gender,
                              @RequestParam(required = false) String description,
                              @RequestParam Double price,
                              @RequestParam(required = false) Double originalPrice,
                              @RequestParam(required = false) String imageUrl,
                              @RequestParam(required = false) String sizes,
                              @RequestParam(required = false) String color,
                              @RequestParam(required = false) Integer stock,
                              @RequestParam(required = false) Boolean active,
                              @RequestParam(required = false) Boolean isNew,
                              RedirectAttributes ra) {

        productService.getProductById(id).ifPresent(p -> {
            p.setName(name);
            p.setBrand(brand);
            p.setCategory(category);
            p.setGender(gender);
            p.setDescription(description);
            p.setPrice(price);
            p.setOriginalPrice(originalPrice);
            p.setImageUrl(imageUrl);
            p.setSizes(sizes);
            p.setColor(color);
            p.setStock(stock != null ? stock : 0);
            p.setActive(active != null && active);
            p.setIsNew(isNew != null && isNew);
            productService.saveProduct(p);
        });

        ra.addFlashAttribute("success", "Product updated successfully!");
        return "redirect:/admin/products";
    }

    // ── Delete Product ────────────────────────────
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        productService.getProductById(id).ifPresent(p -> {
            String name = p.getName();
            productService.deleteProduct(id);
            ra.addFlashAttribute("success", "Product \"" + name + "\" deleted.");
        });
        return "redirect:/admin/products";
    }

    // ── Users List ────────────────────────────────
    @GetMapping("/users")
    public String users(Authentication auth, HttpSession session, Model model) {
        loadAdminSession(auth, session);
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    // ── Toggle User Active/Inactive ───────────────
    @PostMapping("/users/toggle")
    public String toggleUser(@RequestParam Long userId, RedirectAttributes ra) {
        userService.toggleUserStatus(userId);
        ra.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    // ── Management Page ───────────────────────────
    @GetMapping("/management")
    public String management(Authentication auth, HttpSession session, Model model) {
        loadAdminSession(auth, session);
        model.addAttribute("totalProducts", productService.countAllProducts());
        model.addAttribute("totalUsers", userService.countAll());
        model.addAttribute("orders", Collections.emptyList());
        return "admin/management";
    }

    // ── Admin Login Page ──────────────────────────
    @GetMapping("/login")
    public String adminLogin() {
        return "admin/login";
    }
}
