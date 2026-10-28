package com.craveverse.controller;

import com.craveverse.entity.Customer;
import com.craveverse.entity.LoginRequest;
import com.craveverse.entity.RegisterRequest;
import com.craveverse.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController controller;

    @BeforeEach
    void configureAdministratorCredentials() {
        ReflectionTestUtils.setField(controller, "adminEmail", "admin@example.com");
        ReflectionTestUtils.setField(controller, "adminPassword", "strong-secret");
    }

    @Test
    void customerLoginStartsCustomerOnlySession() {
        Customer customer = new Customer();
        customer.setId(42L);
        customer.setName("A Customer");
        customer.setEmail("customer@example.com");
        customer.setPassword("encoded-password");
        when(customerRepository.findByEmail("customer@example.com"))
                .thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("customer-password", "encoded-password"))
                .thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<?> response = controller.customerLogin(
                login("customer@example.com", "customer-password"), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CUSTOMER", ((Map<?, ?>) response.getBody()).get("role"));
        assertEquals(42L, request.getSession(false).getAttribute("customerId"));
        assertEquals(false, request.getSession(false).getAttribute("isAdmin"));
    }

    @Test
    void adminLoginCreatesAdminSessionWithoutCustomerIdentity() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<?> response = controller.adminLogin(
                login("admin@example.com", "strong-secret"), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ADMIN", ((Map<?, ?>) response.getBody()).get("role"));
        assertEquals(true, request.getSession(false).getAttribute("isAdmin"));
        assertNull(request.getSession(false).getAttribute("customerId"));
    }

    @Test
    void customerRegistrationCannotClaimAdministratorEmail() {
        RegisterRequest registration = new RegisterRequest();
        registration.setName("Impersonator");
        registration.setEmail("admin@example.com");
        registration.setPassword("some-password");

        ResponseEntity<?> response = controller.register(registration);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(customerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void adminEmailCannotStartCustomerSession() {
        ResponseEntity<?> response = controller.customerLogin(
                login("admin@example.com", "strong-secret"),
                new MockHttpServletRequest());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(customerRepository, never()).findByEmail("admin@example.com");
    }

    @Test
    void currentSessionReportsAdministratorRole() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("isAdmin", true);
        session.setAttribute("adminName", "Administrator");
        session.setAttribute("adminEmail", "admin@example.com");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);

        ResponseEntity<?> response = controller.getCurrentUser(request);
        Map<?, ?> identity = (Map<?, ?>) response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ADMIN", identity.get("role"));
        assertTrue((Boolean) identity.get("isAdmin"));
    }

    private LoginRequest login(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
