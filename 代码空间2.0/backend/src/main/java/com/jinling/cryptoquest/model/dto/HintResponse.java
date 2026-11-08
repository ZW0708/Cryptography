package com.jinling.cryptoquest.model.dto;

public record HintResponse(int hintLevel, String content, boolean escalateAvailable) {}