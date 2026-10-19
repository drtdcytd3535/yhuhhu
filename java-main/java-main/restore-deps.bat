@echo off
chcp 936 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM  restore-deps.bat  ——  环境依赖恢复（开机重置后自动跑）
REM  做三件事：
REM    1) 恢复 Maven 配置 settings.xml -> C:\Users\Administrator\.m2\
REM    2) 恢复 Maven wrapper 发行版 -> %MAVEN_USER_HOME%\wrapper\
REM    3) 恢复 node_modules / target 快照 -> 项目目录（秒级，免联网）
REM  不依赖 node_modules / target 里的任何内容
REM ============================================================

set "CACHE=D:\ghg-cache"
set "ROOT=%~dp0"
set "FRONTEND=%ROOT%ghg-Vue"
set "BACKEND=%ROOT%springBootTest"
set "M2_DIR=%USERPROFILE%\.m2"
set "MVN_HOME=%CACHE%\maven-home"
set "NPM_CACHE=%CACHE%\npm-cache"

echo [恢复] 检查 D 盘缓存盘 ...
if not exist "%CACHE%" (
    echo [错误] 找不到依赖缓存目录 %CACHE%
    echo        这个目录存放项目依赖，请勿删除。若已丢失需联网重新下载。
    exit /b 1
)

REM ---------- 1) Maven settings.xml ----------
echo [恢复] Maven 配置 settings.xml ...
if not exist "%M2_DIR%" mkdir "%M2_DIR%" >nul 2>&1
if exist "%CACHE%\settings.xml" (
    copy /y "%CACHE%\settings.xml" "%M2_DIR%\settings.xml" >nul 2>&1
)

REM ---------- 2) Maven wrapper 发行版 ----------
REM mvnw 会在 %MAVEN_USER_HOME%\wrapper\dists\ 下找 Maven 本体
echo [恢复] Maven 本体（wrapper dists）...
robocopy "%MVN_HOME%\wrapper" "%M2_DIR%\wrapper" /E /NFL /NDL /NJH /NJS /NP /R:0 /W:0 >nul 2>&1

REM ---------- 3) 前端 node_modules ----------
call :restore_dir "%CACHE%\node_modules" "%FRONTEND%\node_modules" "前端依赖 node_modules"

REM ---------- 4) 后端 target ----------
call :restore_dir "%CACHE%\target" "%BACKEND%\target" "后端编译产物 target"

REM ---------- 5) npm 缓存 ----------
echo [恢复] npm 包缓存 ...
if not exist "%NPM_CACHE%" mkdir "%NPM_CACHE%" >nul 2>&1
robocopy "%NPM_CACHE%" "%LOCALAPPDATA%\npm-cache" /E /NFL /NDL /NJH /NJS /NP /R:0 /W:0 >nul 2>&1

echo [恢复] 完成。
exit /b 0

REM ---------- 子过程：目录快照恢复 ----------
:restore_dir
set "SRC=%~1"
set "DST=%~2"
set "LABEL=%~3"
if not exist "%SRC%" (
    echo [跳过] %LABEL%：快照不存在，稍后需联网安装
    exit /b 0
)
if exist "%DST%" (
    echo [跳过] %LABEL%：已存在，无需恢复
    exit /b 0
)
echo [恢复] %LABEL% ...
robocopy "%SRC%" "%DST%" /E /NFL /NDL /NJH /NJS /NP /R:0 /W:0 >nul 2>&1
exit /b 0
