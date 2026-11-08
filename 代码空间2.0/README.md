# 密码学闯关

面向非技术用户（中学生 / 非专业大学生）的**密码学科普闯关 Web 应用**：用 3—5 个递进式交互关卡，把"为什么公钥可以公开、哈希为什么不可逆、没有认证的密钥交换为什么危险"变成"亲手破解、亲眼所见"的体验。

> 比赛作品 | 华为开发者大赛《金陵数创·工程》（南京工程学院）
> 规划文档：`../密码学闯关-项目规划文档.md`

## 关卡主线

| 关卡 | 名称 | 密码学概念 | 难度 |
| --- | --- | --- | --- |
| 第 1 关 | 凯撒密码破译 | 古典密码 / 频率分析 | ★ |
| 第 2 关 | 哈希的不可逆 | 哈希 / 雪崩效应 / 不可逆性 | ★★ |
| 第 3 关 | 中间人攻击 | Diffie-Hellman / 身份认证的重要性 | ★★★ |
| 第 4 关（可选） | 对称加密积木 | 对称加密 / 混淆与扩散 | ★★★ |
| 第 5 关（可选） | 数字签名挑战 | 哈希 + 非对称 / 防篡改 | ★★★ |

## 技术栈

- **后端**：Java 17 + Spring Boot 3.x + Spring Data JPA + MySQL 8.x（Maven）
- **前端**：Vue 3 + Vite + Pinia + TypeScript（Canvas/SVG 关卡交互）
- **认证**：Spring Security + JWT（Bearer，BCrypt）
- **AI**：华为云 MaaS/盘古主通道，对话网关防腐层统一出口，渐进式提示不泄底
- **部署**：华为云 ECS + Nginx + OBS + Docker 兜底

## 环境要求

- JDK 17+
- Maven 3.9+
- Node 18+（推荐 20+）
- MySQL 8.x（或用 `deploy/docker/docker-compose.yml` 一键起）

## 快速开始

Windows 一键脚本（首次需先 `setup.bat`）：

```bat
setup.bat        :: 安装前端依赖、检查后端环境
start-all.bat    :: 同时起前后端
start-backend.bat
start-frontend.bat
```

手动：

```bash
# 后端（需 MySQL，或改 application-dev.yml 指向 docker-compose 起的库）
cd backend && mvn spring-boot:run

# 前端
cd frontend && npm install && npm run dev
```

Docker 一键起 MySQL + 后端 + 前端：

```bash
cd deploy/docker && docker compose up -d
```

## 目录结构

```
代码空间2.0/
├── backend/      Spring Boot 后端
├── frontend/     Vue 3 前端
├── deploy/       nginx / docker 部署配置
└── AGENTS.md     项目约定（分层/提交/冻结决策）
```

## 端到端主流程

注册登录 → 闯关页（第 1 关谜面）→ 交互解谜 → 卡关唤起 AI 渐进提示 → 校验通过 → 概念总结卡 → 解锁下一关 → 全部通关 → 完成度展示

## 状态

🚧 脚架已生成，业务逻辑逐步填充中。各模块 TODO 见代码内注释。