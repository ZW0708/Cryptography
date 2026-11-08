package com.jinling.cryptoquest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HintRequest(
    @NotNull Long levelId,
    @NotBlank String question,
    Integer currentLevel
) {}