const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
require('dotenv').config();

const app = express();

// ✅ 正确顺序：先配置中间件，再定义路由

// 允许所有跨域请求（放在最前面）
app.use(cors({
    origin: '*',
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));

// 解析JSON请求体
app.use(express.json());

// 静态文件服务（提供图片访问）
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// 数据库连接池（移除无效的配置选项）
const pool = mysql.createPool({
    host: 'localhost',
    user: 'remote_user',
    password: '1234',
    database: 'NeighborLink',
    waitForConnections: true,
    connectionLimit: 10,
    // 移除 acquireTimeout 和 timeout，这些是无效选项
});

// 测试接口
app.get('/api/test', (req, res) => {
    res.json({ message: '✅ NeighborLink API is running!' });
});

// 查询所有用户
app.get('/api/users', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM users');
        res.json(rows);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// 查询所有物品
app.get('/api/items', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM items');
        res.json(rows);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// 根据 user_id 查询用户的物品
app.get('/api/users/:id/items', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM items WHERE user_id = ?', [req.params.id]);
        res.json(rows);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// ✅ 添加图片上传相关路由
// 获取图片列表
app.get('/api/images', async (req, res) => {
    try {
        res.json({ message: '图片接口准备就绪' });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// ✅ 创建uploads目录（如果不存在）
const uploadsDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadsDir)) {
    fs.mkdirSync(uploadsDir);
    console.log('📁 创建uploads目录');
}

// 启动服务器
const PORT = process.env.PORT || 5000;

app.listen(PORT, async () => {
    console.log(`🚀 服务器运行在 http://localhost:${PORT}`);
    console.log(`📁 静态文件服务: http://localhost:${PORT}/uploads/`);
    
    // 数据库连接测试
    try {
        const connection = await pool.getConnection();
        console.log('✅ 数据库连接成功！');
        connection.release();
    } catch (error) {
        console.error('❌ 数据库连接失败:', error.message);
    }
});
