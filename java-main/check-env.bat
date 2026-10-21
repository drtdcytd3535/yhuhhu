@echo off
chcp 936 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM  check-env.bat  ——  启动前环境自检
REM  检查 JDK / Node / MySQL / 端口占用，缺什么直接告诉你
REM  只用系统已装的工具，不依赖 node_modules / target
REM ============================================================

echo ========================================
echo   环境自检
echo ========================================
echo.

set "OK=0"
set "BAD=0"

REM ---------- JDK ----------
java -version >nul 2>&1
if errorlevel 1 (
    echo [X] JDK  : 未找到 java 命令
    echo        需要 JDK 17+，预期路径 C:\Program Files\Zulu\zulu-21
    set /a BAD+=1
) else (
    for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        echo [V] JDK  : %%v
        goto :jdk_done
    )
    :jdk_done
    set /a OK+=1
)

REM ---------- Node ----------
node -v >nul 2>&1
if errorlevel 1 (
    echo [X] Node : 未找到 node 命令
    echo        需要 Node 20+，预期路径 D:\Program Files\nodejs
    set /a BAD+=1
) else (
    for /f %%v in ('node -v') do echo [V] Node : %%v
    set /a OK+=1
)

REM ---------- npm ----------
call npm -v >nul 2>&1
if errorlevel 1 (
    echo [X] npm  : 未找到 npm 命令
    set /a BAD+=1
) else (
    for /f %%v in ('call npm -v 2^>nul') do echo [V] npm  : %%v
    set /a OK+=1
)

REM ---------- MySQL ----------
set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if exist "%MYSQL_BIN%" (
    "%MYSQL_BIN%" -uroot -p123456 -e "SELECT 1;" >nul 2>&1
    if errorlevel 1 (
        echo [!] MySQL: 已安装但连不上（服务未启动或密码不对）
        echo        账号 root / 密码 123456；服务名 MySQL80
        set /a BAD+=1
    ) else (
        echo [V] MySQL: 连接正常（root / 123456）
        set /a OK+=1
    )
) else (
    echo [X] MySQL: 未找到客户端 %MYSQL_BIN%
    set /a BAD+=1
)

echo.

REM ---------- 端口占用 ----------
call :check_port 8081 "后端后端"
call :check_port 5173 "前端前端"

echo.
echo ----------------------------------------
if %BAD% GTR 0 (
    echo 结果：%OK% 项正常，%BAD% 项需处理（见上方 [X] / [!]）
) else (
    echo 结果：全部正常，可以直接启动
)
echo ----------------------------------------
exit /b 0

REM ---------- 子过程：端口检测 ----------
:check_port
set "PORT=%~1"
set "NAME=%~2"
set "BUSY="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do set "BUSY=%%p"
if defined BUSY (
    echo [!] 端口 %PORT%（%NAME%）：已被 PID !BUSY! 占用 —— 可能已在运行
) else (
    echo [V] 端口 %PORT%（%NAME%）：空闲
)
exit /b 0
