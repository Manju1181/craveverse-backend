package com.craveverse.controller;

import com.craveverse.entity.Customer;
import com.craveverse.entity.Order;
import com.craveverse.entity.OrderRepository;
import com.craveverse.repository.CustomerRepository;
import com.craveverse.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerAccessTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderController controller;

    @Test
    void customerOrderListQueriesOnlySessionCustomer() {
        MockHttpServletRequest request = customerRequest(12L);
        when(orderRepository.findByCustomer_Id(12L)).thenReturn(List.of());

        ResponseEntity<?> response = controller.getAllOrders(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderRepository).findByCustomer_Id(12L);
        verify(orderRepository, never()).findAll();
    }

    @Test
    void adminSessionCannotReadCustomerOrderList() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("isAdmin", true);
        request.setSession(session);

        ResponseEntity<?> response = controller.getAllOrders(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(orderRepository, never()).findAll();
        verify(orderRepository, never()).findByCustomer_Id(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void customerCannotReadAnotherCustomersOrderById() {
        Customer owner = new Customer();
        owner.setId(13L);
        Order order = new Order();
        order.setCustomer(owner);
        when(orderRepository.findOrderWithItemsById(99L))
                .thenReturn(Optional.of(order));

        ResponseEntity<?> response = controller.getOrderById(99L, customerRequest(12L));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void customerCanReadTheirOwnOrderById() {
        Customer owner = new Customer();
        owner.setId(12L);
        Order order = new Order();
        order.setCustomer(owner);
        when(orderRepository.findOrderWithItemsById(99L))
                .thenReturn(Optional.of(order));

        ResponseEntity<?> response = controller.getOrderById(99L, customerRequest(12L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(order, response.getBody());
    }

    private MockHttpServletRequest customerRequest(Long customerId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("customerId", customerId);
        session.setAttribute("isAdmin", false);
        request.setSession(session);
        return request;
    }
}
