package com.ayrotek.userauthenticationservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRegistrationRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}
