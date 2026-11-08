package com.jinling.cryptoquest.dialogue_gateway;

public record HintResult(
    int hintLevel,
    String content,
    boolean escalateAvailable,
    String channel,
    long durationMs
) {}