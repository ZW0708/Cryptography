package com.jinling.cryptoquest.model.dto;

import java.util.List;

/**
 * 知识库条目：按关卡 code 组织，帮助用户理解概念、具备闯关能力。
 * solution / answer / analysis 属答案内容，知识库接口不返回（受答案解锁控制）。
 */
public record KnowledgeItem(
    String code,
    String title,
    String concept,
    String overview,
    List<KnowledgeSection> sections,
    List<String> tips,
    List<String> solution,
    String answer,
    String analysis
) {}