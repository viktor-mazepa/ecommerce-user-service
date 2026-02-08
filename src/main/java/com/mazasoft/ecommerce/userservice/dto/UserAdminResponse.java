package com.mazasoft.ecommerce.userservice.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UserAdminResponse(
        UUID id,
                                String userName,
                                String email,
                                String avatar,
                                String firstName,
                                String lastName,
                                LocalDate birthDate,
                                String phoneNumber,
                                String role,
                                UUID kcId) {
}
