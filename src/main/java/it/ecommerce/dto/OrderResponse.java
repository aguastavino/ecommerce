package it.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "An order placed by a customer")
public record OrderResponse(Long id, Long customerId, LocalDateTime createdAt, List<OrderItemResponse> items) {
}
