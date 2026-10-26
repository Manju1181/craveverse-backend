
package com.craveverse.controller;

import com.craveverse.entity.LoginRequest;
import com.craveverse.entity.Customer;
import com.craveverse.entity.RegisterRequest;
import com.craveverse.repository.CustomerRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
        @Value("${app.admin.email:}")
private String adminEmail;

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {

        if (request.getName() == null || request.getName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().length() < 8) {

            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "Name and email are required; password must contain at least 8 characters."));
        }

        String email = request.getEmail().trim().toLowerCase();

        if (customerRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", "An account with this email already exists."));
        }

        Customer customer = new Customer();
        customer.setName(request.getName().trim());
        customer.setEmail(email);
        customer.setPassword(passwordEncoder.encode(request.getPassword()));

        customerRepository.save(customer);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "Registration successful."));
    }

@PostMapping("/login")
public ResponseEntity<?> login(
        @RequestBody LoginRequest request,
        HttpServletRequest httpRequest) {

    if (request.getEmail() == null
            || request.getEmail().isBlank()
            || request.getPassword() == null
            || request.getPassword().isBlank()) {

        return ResponseEntity.badRequest().body(
                Map.of("message", "Email and password are required."));
    }

    String email = request.getEmail().trim().toLowerCase();

    var customerOptional = customerRepository.findByEmail(email)
            .filter(customer ->
                    passwordEncoder.matches(
                            request.getPassword(),
                            customer.getPassword()));

    if (customerOptional.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("message", "Invalid email or password."));
    }

    Customer customer = customerOptional.get();

    // Invalidate an existing session before creating a new one.
    HttpSession existingSession = httpRequest.getSession(false);

    if (existingSession != null) {
        existingSession.invalidate();
    }

    HttpSession session = httpRequest.getSession(true);

    session.setAttribute("customerId", customer.getId());
session.setAttribute("customerName", customer.getName());
session.setAttribute("customerEmail", customer.getEmail());

boolean isAdmin = !adminEmail.isBlank()
        && customer.getEmail().equalsIgnoreCase(adminEmail);

session.setAttribute("isAdmin", isAdmin);

    return ResponseEntity.ok(Map.of(
            "message", "Login successful.",
            "customerId", customer.getId(),
            "name", customer.getName(),
            "email", customer.getEmail()));
}
@PostMapping("/logout")
public ResponseEntity<?> logout(HttpServletRequest request) {

    HttpSession session = request.getSession(false);

    if (session != null) {
        session.invalidate();
    }

    return ResponseEntity.ok(
            Map.of("message", "Logged out successfully."));
}
@GetMapping("/me")
public ResponseEntity<?> getCurrentCustomer(
        HttpServletRequest request) {

    HttpSession session = request.getSession(false);

    if (session == null
            || session.getAttribute("customerId") == null) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("message", "Please log in."));
    }

    return ResponseEntity.ok(Map.of(
            "customerId", session.getAttribute("customerId"),
            "name", session.getAttribute("customerName"),
            "email", session.getAttribute("customerEmail")));
}

}
