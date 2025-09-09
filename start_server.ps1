# MAD Team Project - Python服务器启动脚本
Write-Host "🚀 启动MAD Team Project服务器..." -ForegroundColor Green
Write-Host ""

# 检查Python是否安装
try {
    $pythonVersion = python --version 2>$null
    if ($pythonVersion) {
        Write-Host "✅ Python已安装: $pythonVersion" -ForegroundColor Green
    } else {
        throw "Python未找到"
    }
} catch {
    Write-Host "❌ 错误: 未找到Python" -ForegroundColor Red
    Write-Host "请先安装Python:" -ForegroundColor Yellow
    Write-Host "1. 访问: https://www.python.org/downloads/" -ForegroundColor White
    Write-Host "2. 下载最新版本的Python" -ForegroundColor White
    Write-Host "3. 安装时勾选 'Add Python to PATH'" -ForegroundColor White
    Write-Host "4. 安装完成后重启命令提示符" -ForegroundColor White
    pause
    exit 1
}

Write-Host ""

# 安装Python依赖
Write-Host "📦 安装Python依赖..." -ForegroundColor Blue
try {
    pip install -r requirements.txt
    Write-Host "✅ 依赖安装完成" -ForegroundColor Green
} catch {
    Write-Host "⚠️  依赖安装可能有问题，但继续尝试启动..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🌟 启动服务器..." -ForegroundColor Cyan
Write-Host "按 Ctrl+C 停止服务器" -ForegroundColor Gray
Write-Host ""

# 启动服务器
try {
    python server.py
} catch {
    Write-Host "❌ 服务器启动失败" -ForegroundColor Red
    Write-Host "错误信息: $_" -ForegroundColor Red
    pause
} 