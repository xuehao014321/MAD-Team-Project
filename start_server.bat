@echo off
echo 🚀 启动MAD Team Project服务器...
echo.

REM 检查Python是否安装
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到Python
    echo 请先安装Python: https://www.python.org/downloads/
    echo 安装时请勾选 "Add Python to PATH"
    pause
    exit /b 1
)

echo ✅ Python已安装
echo.

REM 安装依赖
echo 📦 安装Python依赖...
pip install -r requirements.txt

echo.
echo 🌟 启动服务器...
echo 按 Ctrl+C 停止服务器
echo.

REM 启动服务器
python server.py 