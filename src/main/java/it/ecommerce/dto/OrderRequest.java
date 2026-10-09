package it.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Data required to place an order")
public record OrderRequest(
        @Schema(example = "1") @NotNull Long customerId,
        @NotEmpty @Valid List<OrderLineRequest> items) {
}
