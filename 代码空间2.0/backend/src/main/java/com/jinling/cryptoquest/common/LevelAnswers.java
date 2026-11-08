package com.jinling.cryptoquest.common;

import java.util.Map;

/**
 * 各关通关答案注册表：供 DataInitializer（生成哈希）与 OutputFilter（防泄底）共用，
 * 避免答案明文散落多处。新增/修改关卡答案只需改这里。
 */
public final class LevelAnswers {

    private LevelAnswers() {}

    public static final Map<String, String> BY_CODE = Map.of(
        "L1_CAESAR", "MEETMEATTHEOLDLIBRARYATSIXOCLOCKBRINGTHESECRETBOOK",
        "L2_HASH", "collision-found",
        "L3_MITM", "mitm-success",
        "L4_AES", "aes-demo",
        "L5_SIGN", "sign-tamper-detected"
    );
}