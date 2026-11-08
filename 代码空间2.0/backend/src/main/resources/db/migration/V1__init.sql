-- 密码学闯关 · 建表脚本（MySQL 8.x，utf8mb4）
-- JPA ddl-auto=update 会自动建表，此脚本作为显式基线与答辩素材。
CREATE DATABASE IF NOT EXISTS cryptoquest DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cryptoquest;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nickname VARCHAR(32) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(16) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_nickname (nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS levels (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL,
  title VARCHAR(128) NOT NULL,
  difficulty INT NOT NULL,
  crypto_concept VARCHAR(32) NOT NULL,
  intro_text TEXT NOT NULL,
  summary_card TEXT NOT NULL,
  answer_hash VARCHAR(64) NOT NULL,
  unlock_chain VARCHAR(64) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_levels_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_progress (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  level_id BIGINT NOT NULL,
  unlocked TINYINT(1) NOT NULL,
  cleared TINYINT(1) NOT NULL,
  cleared_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_progress_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clearance_records (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  level_id BIGINT NOT NULL,
  duration_sec INT NOT NULL,
  hint_used INT NOT NULL,
  cleared_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_clearance_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_sessions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  level_id BIGINT NOT NULL,
  started_at DATETIME NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS chat_messages (
  id BIGINT NOT NULL AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  role VARCHAR(16) NOT NULL,
  content TEXT NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_msg_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_call_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  channel VARCHAR(16) NOT NULL,
  duration_ms BIGINT NOT NULL,
  success TINYINT(1) NOT NULL,
  result_hint VARCHAR(2048) NULL,
  called_at DATETIME NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS operation_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  action VARCHAR(64) NOT NULL,
  detail VARCHAR(1024) NULL,
  occurred_at DATETIME NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;