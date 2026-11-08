package com.jinling.cryptoquest.model.dto;

public record LevelAdminView(
    Long id, String code, String title, Integer difficulty,
    String cryptoConcept, String introText, String summaryCard, String unlockChain
) {}