@echo off
chcp 65001 >nul
title 密码学闯关 · Java 启动器
cd /d "%~dp0"

set "JAVA_HOME=%~dp0.tools\jdk-17"
set "JAVA_EXE=%~dp0.tools\jdk-17\bin\java.exe"
set "JAVAC_EXE=%~dp0.tools\jdk-17\bin\javac.exe"
set "LAUNCHER_DIR=%~dp0.tools\launcher"

if not exist "%JAVA_EXE%" (
    echo [!] 未找到内置 JDK（.tools\jdk-17），尝试使用系统 JDK
    set "JAVA_HOME="
    where java >nul 2>&1
    if errorlevel 1 (
        echo [X] 未找到 java，请安装 JDK 17+ 后重试
        pause
        exit /b 1
    )
    where javac >nul 2>&1
    if errorlevel 1 (
        echo [X] 未找到 javac（仅 JRE 不够），请安装 JDK 17+ 后重试
        pause
        exit /b 1
    )
    set "JAVA_EXE=java"
    set "JAVAC_EXE=javac"
) else (
    set "PATH=%~dp0.tools\jdk-17\bin;%PATH%"
)

if not exist "%LAUNCHER_DIR%" mkdir "%LAUNCHER_DIR%"

echo [..] 编译 Java 启动器...
"%JAVAC_EXE%" -encoding UTF-8 -d "%LAUNCHER_DIR%" "%~dp0Launcher.java"
if errorlevel 1 (
    echo [X] 启动器编译失败
    pause
    exit /b 1
)

"%JAVA_EXE%" -Dfile.encoding=UTF-8 -cp "%LAUNCHER_DIR%" Launcher
pause