package it.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A line of an order")
public record OrderItemResponse(Long productId, String productCode, int quantity) {
}
