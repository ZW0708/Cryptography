package com.jinling.cryptoquest.api.v1;

import com.jinling.cryptoquest.common.ApiResponse;
import com.jinling.cryptoquest.dialogue_gateway.MaasClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查：db / ai / version（M7 平台工程）。
 * db 用真实连通性探测，ai 报通道是否已配置。
 */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;
    private final MaasClient maasClient;

    @Value("${spring.application.version:0.1.0}")
    private String version;

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("db", checkDb());
        result.put("ai", maasClient.isConfigured() ? "MAAS_READY" : "TEMPLATE_ONLY");
        result.put("version", version);
        return ApiResponse.ok(result);
    }

    private String checkDb() {
        try (var conn = dataSource.getConnection()) {
            return conn.isValid(2) ? "UP" : "DOWN";
        } catch (Exception e) {
            return "DOWN";
        }
    }
}