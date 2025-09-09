# Node.js 自动安装脚本
Write-Host "🚀 开始安装 Node.js..." -ForegroundColor Green
Write-Host ""

# 检查是否已安装 Node.js
try {
    $nodeVersion = node --version 2>$null
    if ($nodeVersion) {
        Write-Host "✅ Node.js 已经安装: $nodeVersion" -ForegroundColor Green
        exit 0
    }
} catch {
    Write-Host "📦 Node.js 未安装，开始下载..." -ForegroundColor Yellow
}

# 尝试使用 winget 安装
Write-Host "🔄 尝试使用 Windows Package Manager 安装..." -ForegroundColor Blue
try {
    winget install OpenJS.NodeJS --accept-source-agreements --accept-package-agreements
    Write-Host "✅ Node.js 安装成功!" -ForegroundColor Green
    Write-Host "请重启命令提示符，然后运行 'node --version' 验证安装" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 自动安装失败" -ForegroundColor Red
    Write-Host ""
    Write-Host "请手动安装 Node.js:" -ForegroundColor Yellow
    Write-Host "1. 打开浏览器访问: https://nodejs.org/" -ForegroundColor White
    Write-Host "2. 点击 'Download Node.js (LTS)' 按钮" -ForegroundColor White
    Write-Host "3. 运行下载的 .msi 文件" -ForegroundColor White
    Write-Host "4. 安装完成后重启命令提示符" -ForegroundColor White
    Write-Host "5. 运行 'node --version' 验证安装" -ForegroundColor White
}

Write-Host ""
Write-Host "安装完成后，你可以运行以下命令启动服务器:" -ForegroundColor Cyan
Write-Host "cd C:\Users\User\Downloads\MAD-Team-Project.hao\MAD-Team-Project.hao" -ForegroundColor White
Write-Host "node server.js" -ForegroundColor White

pause 