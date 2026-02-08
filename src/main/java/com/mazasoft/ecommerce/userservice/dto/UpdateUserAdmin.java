package com.mazasoft.ecommerce.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateUserAdmin (
                              @NotBlank String userName,
                              @NotBlank String email,
                              String avatar,
                              @NotBlank String firstName,
                              @NotBlank String lastName,
                              @NotNull LocalDate birthDate,
                              @NotBlank String phoneNumber,
                              @NotBlank String role) {
}
