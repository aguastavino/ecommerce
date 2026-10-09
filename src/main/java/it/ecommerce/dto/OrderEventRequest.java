package it.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.ecommerce.model.OrderEventType;
import jakarta.validation.constraints.NotNull;

@Schema(description = "An event occurred on an order")
public record OrderEventRequest(@Schema(example = "DELIVERED") @NotNull OrderEventType type) {
}
