package com.jinling.cryptoquest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinling.cryptoquest.model.dto.KnowledgeItem;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * 只读知识库：启动时从 data/knowledge.json 载入内存。
 * 【后续可扩展】迁移到数据库表并支持管理员维护（当前只读、随包发布）。
 */
@Service
public class KnowledgeService {

    private final List<KnowledgeItem> items;

    public KnowledgeService(ObjectMapper objectMapper) {
        try (InputStream in = new ClassPathResource("data/knowledge.json").getInputStream()) {
            KnowledgeFile file = objectMapper.readValue(in, KnowledgeFile.class);
            this.items = file.items() == null ? List.of() : List.copyOf(file.items());
        } catch (IOException e) {
            throw new IllegalStateException("加载知识库失败: data/knowledge.json", e);
        }
    }

    /** 完整内容（含答案），仅供答案解锁服务内部使用，不直接对外暴露。 */
    public List<KnowledgeItem> listFull() {
        return items;
    }

    /** 知识库对外视图：剥离 solution / answer / analysis，避免绕过答案解锁。 */
    public List<KnowledgeItem> listStudy() {
        return items.stream()
            .map(i -> new KnowledgeItem(i.code(), i.title(), i.concept(), i.overview(),
                i.sections(), i.tips(), null, null, null))
            .toList();
    }

    public Optional<KnowledgeItem> findByCode(String code) {
        return items.stream().filter(i -> i.code().equals(code)).findFirst();
    }

    private record KnowledgeFile(List<KnowledgeItem> items) {}
}