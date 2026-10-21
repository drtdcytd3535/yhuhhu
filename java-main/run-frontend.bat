@echo off
chcp 936 >nul
setlocal

REM ============================================================
REM  run-frontend.bat  ——  只启动前端（Vite :5173）
REM  被 start.bat 调用，也可单独运行
REM  不依赖 node_modules 的内容；缺失时会自动恢复/安装
REM ============================================================

set "ROOT=%~dp0"
set "FRONTEND=%ROOT%ghg-Vue"
set "CACHE=D:\ghg-cache"

cd /d "%FRONTEND%"
if errorlevel 1 (
    echo [X] 找不到前端目录 %FRONTEND%
    exit /b 1
)

echo ----------------------------------------
echo  前端启动  Vite  :5173
echo ----------------------------------------

REM node_modules 缺失：先从 D 盘快照恢复，再兜底 npm install
if not exist "node_modules" (
    echo [前端] 缺少 node_modules，尝试从 D 盘快照恢复 ...
    if exist "%CACHE%\node_modules" (
        robocopy "%CACHE%\node_modules" "node_modules" /E /NFL /NDL /NJH /NJS /NP /R:0 /W:0 >nul 2>&1
    )
)

REM 关键：node_modules 里必须有 .bin\vite.cmd，否则 npm run dev 会报
REM "'vite' 不是内部或外部命令"。快照复制经常丢掉 .bin 目录，这里强制补齐。
if not exist "node_modules\.bin\vite.cmd" (
    echo [前端] 缺少 node_modules\.bin（vite 启动器缺失），正在修复 ...
    call npm install --cache "%CACHE%\npm-cache" --prefer-offline --no-audit --no-fund
    if not exist "node_modules\.bin\vite.cmd" (
        echo [X] 修复失败：仍找不到 node_modules\.bin\vite.cmd
        echo     请手动在本目录执行： npm install
        pause
        exit /b 1
    )
    echo [前端] 修复完成。
)

title 前端 Vite :5173
call npm run dev

exit /b 0
