# NeighborLink API 测试工具

## 📋 功能说明

这个测试工具用于验证你的 NeighborLink API 是否能正常读取和操作数据。

## 🚀 快速开始

### 1. 安装依赖
```bash
npm install
```

### 2. 运行完整测试
```bash
npm test
```

### 3. 运行单独测试
```bash
# 只测试API接口
npm run test-api

# 只测试文件上传
npm run test-upload

# 只测试数据库连接
npm run test-db
```

## 🔧 测试内容

### 基础API测试
- ✅ 根路径访问 (`/`)
- ✅ API状态检查 (`/api/test`)
- ✅ 获取所有用户 (`/api/users`)
- ✅ 获取所有物品 (`/api/items`)
- ✅ 获取特定用户物品 (`/api/users/:id/items`)
- ✅ 获取上传文件列表 (`/api/uploads`)

### 数据操作测试
- ✅ 添加新商品 (`POST /api/items`)
- ✅ 更新用户信息 (`PATCH /api/users/:id`)
- ✅ 更新物品信息 (`PATCH /api/items/:id`)

### 文件上传测试
- ✅ 图片文件上传 (`POST /api/upload`)

### 数据库连接测试
- ✅ 用户表查询
- ✅ 物品表查询

## 📝 配置说明

测试工具会自动检测API服务器：
- 优先尝试局域网IP: `http://192.168.0.103:5000`
- 如果失败，则使用localhost: `http://localhost:5000`

## 🎯 测试结果说明

- ✅ 绿色：测试通过
- ❌ 红色：测试失败
- ⚠️ 黄色：警告信息
- ℹ️ 蓝色：信息提示

## 🛠️ 故障排除

### 连接失败
1. 确认API服务器已启动
2. 检查IP地址和端口是否正确
3. 确认防火墙设置允许访问

### 数据库错误
1. 检查MySQL服务是否运行
2. 验证数据库连接配置
3. 确认数据库表结构是否正确

### 文件上传失败
1. 检查uploads目录权限
2. 确认磁盘空间充足
3. 验证文件类型限制设置 