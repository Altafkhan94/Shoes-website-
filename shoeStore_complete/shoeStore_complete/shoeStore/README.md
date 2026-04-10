# Sole & Co — Shoe Store 👟

Spring Boot + Thymeleaf + MySQL e-commerce website.

## Project Structure

```
src/main/java/com/example/demo/
├── ShoeStoreApplication.java       ← Main entry point
├── config/
│   ├── SecurityConfig.java         ← Spring Security setup
│   └── DataInitializer.java        ← Seeds admin user on startup
├── entity/
│   ├── Product.java                ← Product model
│   └── User.java                   ← User model
├── repository/
│   ├── ProductRepository.java      ← Product DB queries
│   └── UserRepository.java         ← User DB queries
├── service/
│   ├── ProductService.java         ← Product business logic
│   ├── UserService.java            ← User business logic
│   └── CustomUserDetailsService.java ← Spring Security auth
└── controller/
    ├── HomeController.java         ← /, /products, /about, /contact
    ├── AuthController.java         ← /login, /signup, /forgot-password
    ├── ProfileController.java      ← /profile/**
    └── AdminController.java        ← /admin/**
```

## Setup & Run

### 1. MySQL Database
```sql
CREATE DATABASE shoe_store_db;
```

### 2. Update credentials in `application.properties`
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Build & Run
```bash
mvn spring-boot:run
```

The app will start at **http://localhost:8080**

Tables are auto-created by Hibernate on first run.

## Default Admin Login

| Field    | Value      |
|----------|------------|
| Username | `admin`    |
| Password | `admin123` |

> ⚠️ Change this password immediately after first login!

## Routes

| Route                    | Access  | Description            |
|--------------------------|---------|------------------------|
| `/`                      | Public  | Home / product showcase |
| `/products`              | Public  | All products, filter by category |
| `/about`                 | Public  | About page             |
| `/contact`               | Public  | Contact page           |
| `/login`                 | Public  | Login                  |
| `/signup`                | Public  | Register               |
| `/forgot-password`       | Public  | Reset password         |
| `/profile`               | User    | View/edit profile      |
| `/profile/change-password` | User  | Change password        |
| `/admin/dashboard`       | Admin   | Admin stats            |
| `/admin/products`        | Admin   | Manage products        |
| `/admin/product-form`    | Admin   | Add/edit product       |
| `/admin/users`           | Admin   | Manage users           |
| `/admin/management`      | Admin   | Order management       |
