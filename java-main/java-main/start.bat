@echo off
chcp 936 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM  start.bat  ——  一键启动（开机重置后直接双击这个）
REM  流程：环境自检 -> 恢复依赖 -> 起后端 -> 等 8081 就绪 -> 起前端
REM  不依赖 node_modules / target 里的任何内容
REM ============================================================

set "ROOT=%~dp0"
set "CACHE=D:\ghg-cache"

title 仙逆登录注册 Demo - 一键启动

echo.
echo ============================================================
echo    仙逆 / 国风 登录注册 Demo    一键启动
echo ============================================================
echo.

REM ---------- 步骤 1：环境自检 ----------
call "%ROOT%check-env.bat"
echo.

REM ---------- 步骤 2：恢复依赖 ----------
call "%ROOT%restore-deps.bat"
if errorlevel 1 (
    echo.
    echo [X] 依赖恢复失败，已中止。
    pause
    exit /b 1
)
echo.

REM ---------- 步骤 3：新窗口启动后端 ----------
echo [启动] 后端 Spring Boot（新窗口）...
start "后端 SpringBoot :8081" cmd /k "%ROOT%run-backend.bat"

REM ---------- 步骤 4：等待 8081 就绪 ----------
echo [等待] 后端就绪中（最多 120 秒）...
set /a WAITED=0
:wait_backend
call :port_listening 8081
if "!READY!"=="1" goto :backend_ok
timeout /t 2 /nobreak >nul
set /a WAITED+=2
if !WAITED! GEQ 120 (
    echo [!] 后端 120 秒仍未就绪，查看后端窗口或 log\xx.log
    echo     仍将继续启动前端。
    goto :start_frontend
)
goto :wait_backend

:backend_ok
echo [成功] 后端已就绪：http://127.0.0.1:8081

:start_frontend
echo.
REM ---------- 步骤 5：新窗口启动前端 ----------
echo [启动] 前端 Vite（新窗口）...
start "前端 Vite :5173" cmd /k "%ROOT%run-frontend.bat"

REM ---------- 步骤 6：等前端后打开浏览器 ----------
echo [等待] 前端就绪中 ...
set /a W2=0
:wait_frontend
call :port_listening 5173
if "!READY!"=="1" goto :frontend_ok
timeout /t 2 /nobreak >nul
set /a W2+=2
if !W2! GEQ 90 goto :frontend_ok
goto :wait_frontend

:frontend_ok
echo [成功] 前端地址：http://127.0.0.1:5173
start "" "http://127.0.0.1:5173"

echo.
echo ============================================================
echo    启动完成
echo      前端  http://127.0.0.1:5173
echo      后端  http://127.0.0.1:8081
echo    停止项目请运行 stop.bat
echo ============================================================
echo.
echo 本窗口可以关闭（两个服务在各自新窗口里跑）。
timeout /t 10 /nobreak >nul
exit /b 0

REM ---------- 子过程：端口监听检测 ----------
:port_listening
set "READY="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%~1 " ^| findstr "LISTENING"') do set "READY=1"
exit /b 0
