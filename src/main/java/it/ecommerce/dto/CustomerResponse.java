package it.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "A registered customer")
public record CustomerResponse(Long id, String firstName, String lastName, LocalDate birthDate,
                               String taxCode, String email) {
}
