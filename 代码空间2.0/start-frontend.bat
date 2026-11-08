@echo off
chcp 65001 >nul
title 密码学闯关 - 前端
cd /d "%~dp0frontend"
if not exist node_modules (
    echo 首次运行，安装依赖...
    call npm install
)
echo 启动前端 Vite 开发服务器（端口 5173）...
npm run dev
pause