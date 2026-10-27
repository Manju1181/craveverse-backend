
package com.craveverse.controller;

import com.craveverse.entity.Food;
import com.craveverse.entity.Order;
import com.craveverse.entity.OrderItem;
import com.craveverse.entity.OrderRepository;
import com.craveverse.repository.CustomerRepository;
import com.craveverse.repository.FoodRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class OrderController {

    private final OrderRepository orderRepository;
    private final FoodRepository foodRepository;
    private final CustomerRepository customerRepository;

    public OrderController(
            OrderRepository orderRepository,
            FoodRepository foodRepository,
            CustomerRepository customerRepository) {

        this.orderRepository = orderRepository;
        this.foodRepository = foodRepository;
        this.customerRepository = customerRepository;
    }

    // Create an order for the logged-in customer
    @PostMapping
    @Transactional
    public ResponseEntity<?> createOrder(
            @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {

        HttpSession session = httpRequest.getSession(false);

        if (session == null
                || session.getAttribute("customerId") == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Please log in before placing an order."
                    ));
        }

        Long customerId =
                ((Number) session.getAttribute("customerId")).longValue();

        if (request.getCustomerName() == null
                || request.getCustomerName().isBlank()
                || request.getPhone() == null
                || !request.getPhone().matches("\\d{10}")
                || request.getAddress() == null
                || request.getAddress().isBlank()
                || request.getItems() == null
                || request.getItems().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body("Please provide valid customer details and order items.");
        }

        var customer =
                customerRepository.findById(customerId).orElse(null);

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Customer account not found. Please log in again."
                    ));
        }

        Order order = new Order();

        // Associate the order with the logged-in customer.
        order.setCustomer(customer);
        order.setCustomerName(customer.getName());
        order.setPhone(request.getPhone());
        order.setAddress(request.getAddress().trim());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest requestedItem : request.getItems()) {

            if (requestedItem == null
                    || requestedItem.getFoodId() == null
                    || requestedItem.getQuantity() == null
                    || requestedItem.getQuantity() <= 0) {

                return ResponseEntity.badRequest()
                        .body("Each item must have a valid food ID and positive quantity.");
            }

            Food food = foodRepository
                    .findById(requestedItem.getFoodId())
                    .orElse(null);

            if (food == null) {
                return ResponseEntity.badRequest()
                        .body("Food not found: "
                                + requestedItem.getFoodId());
            }

            BigDecimal price = BigDecimal.valueOf(food.getPrice())
                    .setScale(2, RoundingMode.HALF_UP);

            OrderItem item = new OrderItem();

            item.setFood(food);
            item.setFoodName(food.getName());
            item.setQuantity(requestedItem.getQuantity());
            item.setPrice(price);

            order.addItem(item);

            BigDecimal itemTotal = price.multiply(
                    BigDecimal.valueOf(requestedItem.getQuantity()));

            total = total.add(itemTotal);
        }

        order.setTotalAmount(
                total.setScale(2, RoundingMode.HALF_UP));

        Order savedOrder = orderRepository.save(order);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedOrder);
    }

    // Return only the logged-in customer's orders
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllOrders(
            HttpServletRequest httpRequest) {

        HttpSession session = httpRequest.getSession(false);

        if (session == null
                || session.getAttribute("customerId") == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Please log in to view your orders."
                    ));
        }

        Long customerId =
                ((Number) session.getAttribute("customerId")).longValue();

        List<Order> orders =
                orderRepository.findByCustomer_Id(customerId);

        return ResponseEntity.ok(orders);
    }

    // Return all orders to authenticated administrators
    @GetMapping("/admin")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllOrdersForAdmin(
            HttpServletRequest httpRequest) {

        HttpSession session = httpRequest.getSession(false);

        if (session == null
                || !Boolean.TRUE.equals(
                        session.getAttribute("isAdmin"))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Administrator access is required to view all orders."
                    ));
        }

        List<AdminOrderResponse> orders = orderRepository.findAll()
                .stream()
                .map(order -> new AdminOrderResponse(
                        order.getId(),
                        order.getCustomerName(),
                        order.getPhone(),
                        order.getAddress(),
                        order.getTotalAmount(),
                        order.getStatus(),
                        order.getCreatedAt(),
                        order.getItems()
                                .stream()
                                .map(item -> {
                                    Food food = item.getFood();

                                    return new AdminOrderResponse
                                            .AdminOrderItemResponse(
                                                    item.getId(),
                                                    food.getId(),
                                                    item.getFoodName(),
                                                    item.getQuantity(),
                                                    item.getPrice()
                                            );
                                })
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(orders);
    }

    // Return an individual order only if it belongs to the logged-in customer
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOrderById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        HttpSession session = httpRequest.getSession(false);

        if (session == null
                || session.getAttribute("customerId") == null) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Please log in to view this order."
                    ));
        }

        Long customerId =
                ((Number) session.getAttribute("customerId")).longValue();

        return orderRepository.findOrderWithItemsById(id)
                .filter(order ->
                        order.getCustomer() != null
                        && order.getCustomer()
                                .getId()
                                .equals(customerId))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update order status for authenticated administrators
    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        HttpSession session = httpRequest.getSession(false);

        if (session == null
                || !Boolean.TRUE.equals(
                        session.getAttribute("isAdmin"))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Administrator access is required to update order status."
                    ));
        }

        String newStatus = request.get("status");

        List<String> allowedStatuses = List.of(
                "PLACED",
                "CONFIRMED",
                "PREPARING",
                "OUT_FOR_DELIVERY",
                "DELIVERED",
                "CANCELLED"
        );

        if (newStatus == null
                || !allowedStatuses.contains(newStatus)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Invalid order status."
                    ));
        }

        Order order = orderRepository.findById(id).orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        String currentStatus = order.getStatus();

        Map<String, List<String>> transitions = Map.of(
                "PLACED", List.of("CONFIRMED", "CANCELLED"),
                "CONFIRMED", List.of("PREPARING", "CANCELLED"),
                "PREPARING", List.of("OUT_FOR_DELIVERY", "CANCELLED"),
                "OUT_FOR_DELIVERY", List.of("DELIVERED"),
                "DELIVERED", List.of(),
                "CANCELLED", List.of()
        );

        if (!transitions.getOrDefault(currentStatus, List.of())
                .contains(newStatus)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Cannot change status from "
                                    + currentStatus
                                    + " to "
                                    + newStatus
                                    + "."
                    ));
        }

        order.setStatus(newStatus);

        Order savedOrder = orderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "id", savedOrder.getId(),
                "status", savedOrder.getStatus(),
                "message", "Order status updated successfully."
        ));
    }
}
