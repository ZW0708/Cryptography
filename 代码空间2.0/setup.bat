@echo off
chcp 65001 >nul
title 密码学闯关 - 环境准备

echo ========================================
echo   密码学闯关 · 环境准备
echo ========================================
echo.

REM ---- 优先使用 .tools 自包含 JDK + Maven ----
set "TOOLS_DIR=%~dp0.tools"
if exist "%TOOLS_DIR%\jdk-17\bin\java.exe" (
    set "JAVA_HOME=%TOOLS_DIR%\jdk-17"
    set "PATH=%TOOLS_DIR%\jdk-17\bin;%TOOLS_DIR%\maven\bin;%PATH%"
    echo [√] 使用 .tools 自包含环境
    echo     JDK:  %TOOLS_DIR%\jdk-17
    echo     Maven: %TOOLS_DIR%\maven
) else (
    echo [!] 未找到 .tools 自包含环境，使用系统 PATH
)

REM ---- 检查 JDK 17 ----
where java >nul 2>nul
if errorlevel 1 (
    echo [X] 未检测到 java，请先安装 JDK 17 或将 JDK 复制到 .tools\jdk-17
    pause
    exit /b 1
)
java -version 2>&1 | findstr /R "17\." >nul
if errorlevel 1 (
    echo [!] 警告：未确认 JDK 17，请确保 java -version 为 17.x
) else (
    echo [√] JDK 17 已就绪
)

REM ---- 检查 Maven ----
where mvn >nul 2>nul
if errorlevel 1 (
    echo [X] 未检测到 mvn，请先安装 Maven 3.8+ 或将 Maven 复制到 .tools\maven
    pause
    exit /b 1
) else (
    echo [√] Maven 已就绪
)

REM ---- 检查 Node ----
where node >nul 2>nul
if errorlevel 1 (
    echo [X] 未检测到 node，请先安装 Node 18+ 并加入 PATH
    pause
    exit /b 1
) else (
    echo [√] Node 已就绪
)

REM ---- 前端依赖 ----
echo.
echo [..] 安装前端依赖...
cd /d "%~dp0frontend"
call npm install
if errorlevel 1 (
    echo [X] 前端依赖安装失败
    pause
    exit /b 1
)
echo [√] 前端依赖已安装

echo.
echo ========================================
echo   环境准备完成
echo   后端启动前请确保 MySQL 已运行并可连接
echo   （可用 deploy\docker\docker-compose.yml 一键起 MySQL）
echo ========================================
pause