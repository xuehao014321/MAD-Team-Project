# MAD Team Project - 服务器设置指南

## 快速启动

### 1. 启动Python服务器
双击运行 `run_server.bat` 文件，或在命令行中执行：
```bash
py server.py
```

服务器将在 `http://192.168.0.104:5000` 启动

### 2. Android应用连接
Android应用已配置为连接到 `http://192.168.0.104:5000/api/`

## API端点

服务器提供以下API端点：

- **测试连接**: `GET /api/test`
- **用户注册**: `POST /api/users`
- **用户登录**: `POST /api/login`
- **获取所有用户**: `GET /api/users`
- **获取特定用户**: `GET /api/users/<user_id>`

## 测试步骤

1. **启动服务器**
   - 运行 `run_server.bat`
   - 确认看到 "服务器启动在 http://192.168.0.104:5000"

2. **测试Android应用**
   - 在Android应用中点击"Debug"按钮测试服务器连接
   - 尝试注册新用户
   - 尝试登录

## 数据存储

- 用户数据存储在 `users.json` 文件中
- 服务器会自动创建和管理此文件

## 预设测试账户

服务器启动时会自动创建以下测试账户：

1. **测试用户**
   - 用户名: testuser
   - 邮箱: test@example.com
   - 密码: 123456
   - 手机: 1234567890

2. **管理员**
   - 用户名: admin
   - 邮箱: admin@example.com
   - 密码: admin123
   - 手机: 0987654321

## 故障排除

### 连接失败
1. 确认服务器正在运行
2. 检查设备是否在同一网络
3. 检查防火墙设置
4. 确认IP地址 192.168.0.104 是正确的

### 修改IP地址
如果需要修改IP地址：
1. 编辑 `server.py` 中的 `HOST` 变量
2. 编辑 `app/src/main/java/com/example/mad_gruop_ass/api/RetrofitClient.kt` 中的 `BASE_URL`

## 依赖项

确保已安装以下Python包：
- Flask
- Flask-CORS

安装命令：
```bash
pip install flask flask-cors
```

或使用requirements.txt：
```bash
pip install -r requirements.txt
``` 