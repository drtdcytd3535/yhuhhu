@echo off
chcp 936 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM  run-backend.bat  ——  只启动后端（Spring Boot :8081）
REM  被 start.bat 调用，也可单独运行
REM  不依赖 node_modules / target 的内容（target 缺失会自动编译）
REM ============================================================

set "ROOT=%~dp0"
set "BACKEND=%ROOT%springBootTest"
set "CACHE=D:\ghg-cache"
set "MVN_HOME=%CACHE%\maven-home"

REM 本地仓库在 %USERPROFILE%\.m2\repository（restore-deps.bat 已把 wrapper 恢复到这里）
REM 之前指向 %MVN_HOME% 但那个目录里没有 repository，导致离线模式必然失败
set "MAVEN_USER_HOME=%USERPROFILE%\.m2"

cd /d "%BACKEND%"
if errorlevel 1 (
    echo [X] 找不到后端目录 %BACKEND%
    exit /b 1
)

REM ---------- 启动前自动清理 8081 端口 ----------
REM 之前报错 "Port 8081 was already in use"，是上次启动残留的 java 进程没退干净。
REM 现在每次启动前先按端口找到残留进程并结束，保证端口空闲。
echo [清理] 检查并释放 8081 端口 ...
set "BUSY="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8081 " ^| findstr "LISTENING"') do (
    set "BUSY=%%p"
    echo [清理] 端口 8081 被进程 %%p 占用，正在结束 ...
    taskkill /F /PID %%p >nul 2>&1
)
if defined BUSY (
    timeout /t 2 /nobreak >nul
    echo [清理] 已释放 8081 端口。
) else (
    echo [清理] 8081 端口空闲。
)

echo ----------------------------------------
echo  后端启动  Spring Boot  :8081
echo ----------------------------------------

REM 优先跑离线模式（依赖都在 D 盘仓库），失败再走联网
echo [后端] 校验依赖并编译 ...
call mvnw.cmd -o -B -DskipTests clean package > "%TEMP%\backend-build.log" 2>&1
if errorlevel 1 (
    echo [后端] 离线模式失败，改用联网模式重新构建 ...
    call mvnw.cmd -B -DskipTests clean package > "%TEMP%\backend-build.log" 2>&1
    if errorlevel 1 (
        echo [X] 后端构建失败，详细日志见 %TEMP%\backend-build.log
        exit /b 1
    )
)
echo [后端] 构建完成。

echo [后端] 启动中（日志：springBootTest\log\xx.log）...
title 后端 SpringBoot :8081
REM 用命令行参数把端口钉死在 8081：环境变量 SERVER_PORT / SERVER__PORT
REM 之类会被 Spring Boot 宽松绑定捡走，导致 Tomcat 起在别的端口上
call mvnw.cmd -o -B -DskipTests spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

exit /b 0
