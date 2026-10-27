package com.craveverse.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderResponse(
        Long id,
        String customerName,
        String phone,
        String address,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt,
        List<AdminOrderItemResponse> items
) {
    public record AdminOrderItemResponse(
            Long id,
            Long foodId,
            String foodName,
            Integer quantity,
            BigDecimal price
    ) {}
}