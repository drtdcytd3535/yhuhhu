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

REM Maven wrapper 的发行版与配置都从 D 盘读取
set "MAVEN_USER_HOME=%MVN_HOME%"

cd /d "%BACKEND%"
if errorlevel 1 (
    echo [X] 找不到后端目录 %BACKEND%
    exit /b 1
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
call mvnw.cmd -o -B -DskipTests spring-boot:run

exit /b 0
