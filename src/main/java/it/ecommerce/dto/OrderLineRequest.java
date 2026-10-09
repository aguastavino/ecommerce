package it.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "A requested product with its quantity")
public record OrderLineRequest(
        @Schema(example = "1") @NotNull Long productId,
        @Schema(example = "2") @Min(1) int quantity) {
}
