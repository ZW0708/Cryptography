package com.jinling.cryptoquest;

import com.jinling.cryptoquest.model.dto.LevelAdminView;
import com.jinling.cryptoquest.model.dto.LevelUpdateRequest;
import com.jinling.cryptoquest.service.AdminService;
import com.jinling.cryptoquest.api.v1.HealthController;
import com.jinling.cryptoquest.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * admin 关卡文案 CRUD + 健康检查（db 真实探测 / ai 通道状态）。
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminHealthTest {

    @Autowired AdminService adminService;
    @Autowired HealthController healthController;

    @Test
    void adminListsAndUpdateLevel() {
        List<LevelAdminView> levels = adminService.listLevels();
        assertEquals(5, levels.size());
        LevelAdminView l1 = levels.stream().filter(l -> l.code().equals("L1_CAESAR")).findFirst().orElseThrow();

        LevelAdminView updated = adminService.updateLevel(l1.id(),
            new LevelUpdateRequest("凯撒密码破译（改）", "新谜面", "新总结卡"));
        assertEquals("凯撒密码破译（改）", updated.title());
        assertEquals("新谜面", updated.introText());
        assertEquals("新总结卡", updated.summaryCard());

    }

    @Test
    void healthReportsDbAndAi() {
        ApiResponse<Map<String, Object>> resp = healthController.health();
        Map<String, Object> data = resp.data();
        assertEquals("UP", data.get("status"));
        assertEquals("UP", data.get("db"), "H2 应连通");
        assertEquals("TEMPLATE_ONLY", data.get("ai"), "未配置 MAAS 应报 TEMPLATE_ONLY");
        assertNotNull(data.get("version"));
    }
}