package com.jinling.cryptoquest.model.dto;

public record LevelView(
    Long id,
    String code,
    String title,
    Integer difficulty,
    String cryptoConcept,
    String introText,
    Boolean unlocked,
    Boolean cleared
) {}