@echo off
chcp 65001 >nul
title 密码学闯关 - 后端
cd /d "%~dp0"

REM ---- 优先使用 .tools 自包含环境 ----
set "JAVA_HOME=%~dp0.tools\jdk-17"
set "PATH=%~dp0.tools\jdk-17\bin;%~dp0.tools\maven\bin;%PATH%"
set "MVN_CMD=%~dp0.tools\maven\bin\mvn.cmd"
if not exist "%MVN_CMD%" (
    REM 回退到系统环境
    set "MVN_CMD=mvn"
    echo [!] 未找到 .tools 自包含环境，回退到系统 mvn
)

cd /d "%~dp0backend"
echo 启动后端 Spring Boot（端口 8080）...
echo 请确保 MySQL 已运行（见 application-dev.yml 配置）
"%MVN_CMD%" spring-boot:run
pause