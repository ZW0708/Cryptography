package com.jinling.cryptoquest.dialogue_gateway;

/**
 * 第三方降级备份通道。开关默认关闭（D-03 冻结决策）。
 */
public class FallbackChannel {
    public String call(HintContext ctx) {
        throw new UnsupportedOperationException("降级通道默认关闭");
    }
}