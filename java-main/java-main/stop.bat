@echo off
chcp 936 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM  stop.bat  ——  一键停止前后端
REM  按端口 5173 / 8081 找到进程并结束
REM ============================================================

title 停止项目

echo.
echo ============================================================
echo    停止 仙逆登录注册 Demo
echo ============================================================
echo.

call :kill_port 5173 "前端 Vite"
call :kill_port 8081 "后端 SpringBoot"

echo.
echo [完成] 已停止占用 5173 / 8081 的进程。
echo.
timeout /t 4 /nobreak >nul
exit /b 0

REM ---------- 子过程：按端口结束进程 ----------
:kill_port
set "PORT=%~1"
set "NAME=%~2"
set "FOUND="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
    set "FOUND=1"
    echo [停止] %NAME%（端口 %PORT%）PID %%p
    taskkill /F /PID %%p >nul 2>&1
)
if not defined FOUND echo [跳过] %NAME%（端口 %PORT%）未在运行
exit /b 0
