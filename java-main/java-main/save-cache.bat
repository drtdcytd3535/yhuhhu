@echo off
chcp 936 >nul
setlocal

REM ============================================================
REM  save-cache.bat  ——  更新 D 盘依赖快照
REM  什么时候用：改过 package.json / pom.xml，或新装了依赖之后
REM  作用：把当前的 node_modules / target / maven 仓库 存回 D 盘
REM  平时不用跑，只有依赖变化时跑一次即可
REM ============================================================

set "ROOT=%~dp0"
set "CACHE=D:\ghg-cache"
set "FRONTEND=%ROOT%ghg-Vue"
set "BACKEND=%ROOT%springBootTest"

title 更新依赖快照

echo.
echo ============================================================
echo    更新 D 盘依赖快照  ->  %CACHE%
echo ============================================================
echo.

if not exist "%CACHE%" mkdir "%CACHE%" >nul 2>&1

echo [1/5] 前端 node_modules ...
if exist "%FRONTEND%\node_modules" (
    robocopy "%FRONTEND%\node_modules" "%CACHE%\node_modules" /E /NFL /NDL /NJH /NJS /NP /R:1 /W:1 >nul 2>&1
    echo       已更新
) else (
    echo       跳过（node_modules 不存在，请先启动一次前端）
)

echo [2/5] 后端 target ...
if exist "%BACKEND%\target" (
    robocopy "%BACKEND%\target" "%CACHE%\target" /E /NFL /NDL /NJH /NJS /NP /R:1 /W:1 >nul 2>&1
    echo       已更新
) else (
    echo       跳过（target 不存在，请先启动一次后端）
)

echo [3/5] Maven 本地仓库 ...
if exist "%USERPROFILE%\.m2\repository" (
    robocopy "%USERPROFILE%\.m2\repository" "%CACHE%\m2repo" /E /NFL /NDL /NJH /NJS /NP /R:1 /W:1 >nul 2>&1
    echo       已更新
) else (
    echo       跳过（.m2\repository 不存在）
)

echo [4/5] Maven wrapper 发行版 ...
if exist "%USERPROFILE%\.m2\wrapper" (
    robocopy "%USERPROFILE%\.m2\wrapper" "%CACHE%\maven-home\wrapper" /E /NFL /NDL /NJH /NJS /NP /R:1 /W:1 >nul 2>&1
    echo       已更新
) else (
    echo       跳过（.m2\wrapper 不存在）
)

echo [5/5] Maven 配置 + npm 缓存 ...
if exist "%USERPROFILE%\.m2\settings.xml" copy /y "%USERPROFILE%\.m2\settings.xml" "%CACHE%\settings.xml" >nul 2>&1
robocopy "%LOCALAPPDATA%\npm-cache" "%CACHE%\npm-cache" /E /NFL /NDL /NJH /NJS /NP /R:1 /W:1 >nul 2>&1
echo       已更新

echo.
echo ============================================================
echo    快照更新完成
echo ============================================================
echo.
timeout /t 5 /nobreak >nul
exit /b 0
