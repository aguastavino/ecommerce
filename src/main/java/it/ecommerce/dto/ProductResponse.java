package it.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A product")
public record ProductResponse(Long id, String code, String name, int stock) {
}
