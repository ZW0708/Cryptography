package com.jinling.cryptoquest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerSubmitRequest(
    @NotNull Long levelId,
    @NotBlank String answer,
    Integer durationSec,
    Integer hintUsed
) {}