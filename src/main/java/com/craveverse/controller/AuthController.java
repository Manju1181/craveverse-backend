package com.craveverse.controller;

import com.craveverse.entity.Customer;
import com.craveverse.entity.LoginRequest;
import com.craveverse.entity.RegisterRequest;
import com.craveverse.repository.CustomerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getName() == null || request.getName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message",
                    "Name and email are required; password must contain at least 8 characters."));
        }

        String email = request.getEmail().trim().toLowerCase();
        if (isAdminEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "This email is reserved for administrator sign-in."));
        }
        if (customerRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "An account with this email already exists."));
        }

        Customer customer = new Customer();
        customer.setName(request.getName().trim());
        customer.setEmail(email);
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customerRepository.save(customer);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Registration successful."));
    }

    /** Keep the existing customer login route as a compatibility alias. */
    @PostMapping({"/login", "/customer/login"})
    public ResponseEntity<?> customerLogin(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Email and password are required."));
        }

        String email = request.getEmail().trim().toLowerCase();
        if (isAdminEmail(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "This is an administrator account. Use administrator sign-in."));
        }

        Customer customer = customerRepository.findByEmail(email)
                .filter(account -> passwordEncoder.matches(
                        request.getPassword(), account.getPassword()))
                .orElse(null);
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Invalid email or password."));
        }

        HttpSession session = startFreshSession(httpRequest);
        session.setAttribute("customerId", customer.getId());
        session.setAttribute("customerName", customer.getName());
        session.setAttribute("customerEmail", customer.getEmail());
        session.setAttribute("isAdmin", false);

        return ResponseEntity.ok(Map.of(
                "message", "Login successful.",
                "customerId", customer.getId(),
                "name", customer.getName(),
                "email", customer.getEmail(),
                "isAdmin", false,
                "role", "CUSTOMER"));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Email and password are required."));
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "message", "Administrator sign-in is not configured on the backend."));
        }

        String email = request.getEmail().trim().toLowerCase();
        boolean validPassword = MessageDigest.isEqual(
                request.getPassword().getBytes(StandardCharsets.UTF_8),
                adminPassword.getBytes(StandardCharsets.UTF_8));
        if (!isAdminEmail(email) || !validPassword) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Invalid administrator email or password."));
        }

        HttpSession session = startFreshSession(httpRequest);
        session.setAttribute("isAdmin", true);
        session.setAttribute("adminName", "Administrator");
        session.setAttribute("adminEmail", adminEmail.trim());

        return ResponseEntity.ok(Map.of(
                "message", "Administrator login successful.",
                "name", "Administrator",
                "email", adminEmail.trim(),
                "isAdmin", true,
                "role", "ADMIN"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean isAdmin = session != null
                && Boolean.TRUE.equals(session.getAttribute("isAdmin"));
        if (session == null
                || (!isAdmin && session.getAttribute("customerId") == null)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Please log in."));
        }

        Map<String, Object> identity = new HashMap<>();
        Object customerId = session.getAttribute("customerId");
        if (customerId != null) {
            identity.put("customerId", customerId);
        }
        identity.put("name", isAdmin
                ? session.getAttribute("adminName")
                : session.getAttribute("customerName"));
        identity.put("email", isAdmin
                ? session.getAttribute("adminEmail")
                : session.getAttribute("customerEmail"));
        identity.put("isAdmin", isAdmin);
        identity.put("role", isAdmin ? "ADMIN" : "CUSTOMER");
        return ResponseEntity.ok(identity);
    }

    private boolean isAdminEmail(String email) {
        return adminEmail != null
                && !adminEmail.isBlank()
                && email.equalsIgnoreCase(adminEmail.trim());
    }

    private HttpSession startFreshSession(HttpServletRequest request) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }
        return request.getSession(true);
    }
}