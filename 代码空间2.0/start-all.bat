@echo off
chcp 65001 >nul
title 密码学闯关 - 全栈启动
echo 启动后端（新窗口）...
start "密码学闯关-后端" cmd /k "%~dp0start-backend.bat"
echo 启动前端（新窗口）...
start "密码学闯关-前端" cmd /k "%~dp0start-frontend.bat"
echo.
echo 后端: http://localhost:8080  接口文档: http://localhost:8080/swagger-ui.html
echo 前端: http://localhost:5173
echo.
pause