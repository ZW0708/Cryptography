package com.jinling.cryptoquest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank @Size(min = 2, max = 32) String nickname,
    @NotBlank @Size(min = 6, max = 64) String password
) {}