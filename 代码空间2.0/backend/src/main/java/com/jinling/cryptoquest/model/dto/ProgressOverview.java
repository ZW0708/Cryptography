package com.jinling.cryptoquest.model.dto;

import java.util.List;

public record ProgressOverview(int totalLevels, int clearedCount, List<LevelView> levels) {}