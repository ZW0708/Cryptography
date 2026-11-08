# 密码学闯关 · 项目约定

> 比赛作品 | 华为开发者大赛《金陵数创·工程》
> 规划基线：`../密码学闯关-项目规划文档.md`

## 技术栈
- 后端：Java 17 + Spring Boot 3.x + Spring Data JPA + MySQL 8.x（Maven 构建）
- 前端：Vue 3 + Vite + Pinia + TypeScript
- 认证：Spring Security + JWT（Bearer，无 Cookie，BCrypt 哈希存储）
- AI：华为云 MaaS/盘古主通道，对话网关防腐层统一出口；码道智能体编排留痕；第三方降级开关默认关闭
- 接口文档：springdoc-openapi（答辩素材）
- 部署：华为云 ECS（c7.large.2）+ Nginx + OBS + Docker 兜底

## 开发命令
- 后端：`cd backend && mvn spring-boot:run`
- 前端：`cd frontend && npm install && npm run dev`
- 后端构建：`cd backend && mvn clean package -DskipTests`
- 后端测试：`cd backend && mvn test`
- seed 关卡：`cd backend && mvn spring-boot:run -Dspring-boot.run.arguments=--seed`（待实现）

## 分层约定（后端）
- `api/v1/` 控制器层：协议转换 + 校验，不含业务逻辑
- `service/` 服务层：业务逻辑
- `dialogue_gateway/` 防腐层：唯一 AI 出口（MAAS 主 / MADAO 留痕 / FALLBACK 备份三通道隔离）
- `model/entity/` JPA 实体，`model/repo/` Spring Data JPA 仓库，禁手拼 SQL
- `security/` JWT 过滤器与工具
- 依赖方向：api → service → {model, dialogue_gateway, security}，禁止环形依赖

## 关卡引擎约定
- 关卡配置由 `resources/data/levels.json` 驱动（谜面/答案哈希/解锁链/AI 提示素材）
- 答案哈希比对在服务端，前端隐藏不作安全边界
- 每关统一结构：谜面引入 → 交互解谜 → 概念总结卡 → AI 渐进式提示（L1→L2→L3，永不直接给答案）

## 提交规范
Conventional Commits：feat / fix / docs / chore
- main 保护分支 + feature 分支 + PR 门禁
- 每周冻结标签

## 冻结决策（不得推翻）
- D-01：MySQL 8.x，连接串参数含 useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
- D-02：JWT Bearer 无 Cookie，最小采集（账号+密码）
- D-03：AI 降级开关默认 false，不暴露 UI；渐进式提示由提示词模板 + 输出后置过滤双防线保证不泄底
- D-04：哈希演示使用真实 SHA-256 截断展示，不用虚构算法
- D-05：关卡 1—3 为核心必交付，4—5 可选取舍

## 概念准确性红线
- 每关建立概念校对清单：概念表述/示例数据/总结卡文案三项注明出处
- 比喻与简化必须标注"简化演示，实际算法更复杂"