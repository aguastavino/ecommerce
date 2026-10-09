package it.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data required to add a product")
public record ProductRequest(
        @Schema(example = "P-001") @NotBlank String code,
        @Schema(example = "Wireless mouse") @NotBlank String name,
        @Schema(description = "Available quantity", example = "10") @Min(0) int stock) {
}
