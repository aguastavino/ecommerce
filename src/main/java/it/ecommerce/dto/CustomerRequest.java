package it.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Schema(description = "Data required to register a customer")
public record CustomerRequest(
        @Schema(example = "Mario") @NotBlank String firstName,
        @Schema(example = "Rossi") @NotBlank String lastName,
        @Schema(example = "1990-01-31") @NotNull @Past LocalDate birthDate,
        @Schema(example = "RSSMRA90A31H501Z") @NotBlank @Pattern(regexp = "^[A-Za-z0-9]{16}$",
                message = "must be 16 alphanumeric characters") String taxCode,
        @Schema(example = "mario.rossi@example.com") @NotBlank @Email String email) {
}
