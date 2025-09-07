const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const multer = require('multer');
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

// 创建物品（从客户端接收 JSON）
app.post('/api/items', async (req, res) => {
    try {
        const { title, description, price, image_url, status, user_id, views, likes, distance } = req.body;
        const [result] = await pool.query(
            'INSERT INTO items (user_id, title, description, price, image_url, status, views, likes, distance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [user_id, title, description, price, image_url, status, views, likes, distance]
        );
        res.status(201).json({ success: true, item_id: result.insertId });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
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

// 图片上传接口（表单字段名：image）
app.post('/api/upload', upload.single('image'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ success: false, message: 'No file uploaded' });
        }
        const fileUrl = `${req.protocol}://${req.get('host')}/uploads/${req.file.filename}`;
        return res.json({ success: true, image_url: fileUrl });
    } catch (error) {
        return res.status(500).json({ success: false, message: error.message });
    }
});

// ✅ 创建uploads目录（如果不存在）
const uploadsDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadsDir)) {
    fs.mkdirSync(uploadsDir);
    console.log('📁 创建uploads目录');
}

// 配置 multer 用于处理文件上传
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, uploadsDir);
    },
    filename: function (req, file, cb) {
        const timestamp = Date.now();
        const originalName = file.originalname || 'image.jpg';
        const ext = path.extname(originalName) || '.jpg';
        cb(null, `${timestamp}${ext}`);
    }
});

const upload = multer({ storage });

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
