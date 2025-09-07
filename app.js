const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const multer = require('multer');
const os = require('os');
require('dotenv').config();

const app = express();

// ✅ 获取局域网IP地址
function getLocalIP() {
    const interfaces = os.networkInterfaces();
    for (const name of Object.keys(interfaces)) {
        for (const interface of interfaces[name]) {
            if (interface.family === 'IPv4' && !interface.internal) {
                return interface.address;
            }
        }
    }
    return 'localhost';
}

const LOCAL_IP = getLocalIP();

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

// ✅ 创建uploads目录（如果不存在）
const uploadsDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadsDir)) {
    fs.mkdirSync(uploadsDir);
    console.log('📁 创建uploads目录');
}

// ✅ Multer配置
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, uploadsDir);
    },
    filename: function (req, file, cb) {
        const uniqueName = Date.now() + '-' + Math.round(Math.random() * 1E9) + path.extname(file.originalname);
        cb(null, uniqueName);
    }
});

const fileFilter = (req, file, cb) => {
    const allowedTypes = /jpeg|jpg|png|gif|webp/;
    const extname = allowedTypes.test(path.extname(file.originalname).toLowerCase());
    const mimetype = allowedTypes.test(file.mimetype);

    if (mimetype && extname) {
        return cb(null, true);
    } else {
        cb(new Error('只允许上传图片文件'), false);
    }
};

const upload = multer({
    storage: storage,
    limits: {
        fileSize: 5 * 1024 * 1024 // 5MB限制
    },
    fileFilter: fileFilter
});

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

// 查询所有物品（包含图片URL处理）
app.get('/api/items', async (req, res) => {
    try {
        const [rows] = await pool.query(`
            SELECT 
                i.*,
                u.username,
                CASE 
                    WHEN i.image_url IS NOT NULL AND i.image_url != '' 
                    THEN CONCAT('http://${LOCAL_IP}:${PORT}/uploads/', i.image_url)
                    ELSE NULL 
                END as image_url
            FROM items i 
            LEFT JOIN users u ON i.user_id = u.user_id 
            ORDER BY i.created_at DESC
        `);
        
        console.log(`📦 查询到 ${rows.length} 个物品`);
        res.json(rows);
    } catch (error) {
        console.error('❌ 查询物品错误:', error);
        res.status(500).json({ error: error.message });
    }
});

// 根据 user_id 查询用户的物品（包含图片URL处理）
app.get('/api/users/:id/items', async (req, res) => {
    try {
        const [rows] = await pool.query(`
            SELECT 
                i.*,
                u.username,
                CASE 
                    WHEN i.image_url IS NOT NULL AND i.image_url != '' 
                    THEN CONCAT('http://${LOCAL_IP}:${PORT}/uploads/', i.image_url)
                    ELSE NULL 
                END as image_url
            FROM items i 
            LEFT JOIN users u ON i.user_id = u.user_id 
            WHERE i.user_id = ? 
            ORDER BY i.created_at DESC
        `, [req.params.id]);
        
        console.log(`👤 用户 ${req.params.id} 有 ${rows.length} 个物品`);
        res.json(rows);
    } catch (error) {
        console.error('❌ 查询用户物品错误:', error);
        res.status(500).json({ error: error.message });
    }
});

// 根据 item_id 查询单个物品详情（包含图片URL处理）
app.get('/api/items/:id', async (req, res) => {
    try {
        const [rows] = await pool.query(`
            SELECT 
                i.*,
                u.username,
                u.email,
                u.phone,
                CASE 
                    WHEN i.image_url IS NOT NULL AND i.image_url != '' 
                    THEN CONCAT('http://${LOCAL_IP}:${PORT}/uploads/', i.image_url)
                    ELSE NULL 
                END as image_url
            FROM items i 
            LEFT JOIN users u ON i.user_id = u.user_id 
            WHERE i.item_id = ?
        `, [req.params.id]);
        
        if (rows.length === 0) {
            return res.status(404).json({ error: '物品不存在' });
        }
        
        console.log(`📦 查询物品详情: ${rows[0].title}`);
        res.json(rows[0]);
    } catch (error) {
        console.error('❌ 查询物品详情错误:', error);
        res.status(500).json({ error: error.message });
    }
});

// ✅ 创建物品接口（支持图片上传）
app.post('/api/items', upload.single('image'), async (req, res) => {
    try {
        const { user_id, title, description, price, status = 'Available' } = req.body;
        
        if (!user_id || !title || !description || !price) {
            return res.status(400).json({ 
                success: false,
                error: '缺少必要字段: user_id, title, description, price' 
            });
        }

        // 处理图片URL
        let imageUrl = null;
        if (req.file) {
            imageUrl = req.file.filename; // 只存储文件名，完整URL在查询时生成
            console.log('📷 物品包含图片:', req.file.filename);
        }

        const [result] = await pool.query(`
            INSERT INTO items (user_id, title, description, price, image_url, status, views, likes, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, 0, 0, NOW())
        `, [user_id, title, description, price, imageUrl, status]);

        const itemId = result.insertId;
        
        console.log(`✅ 创建物品成功: ID=${itemId}, 标题=${title}`);
        
        res.json({
            success: true,
            message: '物品创建成功',
            item_id: itemId,
            image_url: imageUrl ? `http://${LOCAL_IP}:${PORT}/uploads/${imageUrl}` : null
        });

    } catch (error) {
        console.error('❌ 创建物品错误:', error);
        res.status(500).json({
            success: false,
            error: error.message
        });
    }
});

// ✅ 文件上传接口
app.post('/api/upload', upload.single('image'), (req, res) => {
    try {
        console.log('📤 收到上传请求:', {
            hasFile: !!req.file,
            originalname: req.file?.originalname,
            mimetype: req.file?.mimetype,
            size: req.file?.size
        });

        if (!req.file) {
            console.log('❌ 没有文件或文件被拒绝');
            return res.status(400).json({ 
                success: false,
                error: '没有选择文件或文件类型不正确' 
            });
        }

        // ✅ 修复：使用局域网IP生成URL
        const imageUrl = `http://${LOCAL_IP}:${PORT}/uploads/${req.file.filename}`;

        console.log('✅ 文件上传成功:', {
            originalname: req.file.originalname,
            filename: req.file.filename,
            imageUrl: imageUrl
        });

        res.json({
            success: true,
            message: '文件上传成功',
            filename: req.file.filename,
            originalname: req.file.originalname,
            path: `/uploads/${req.file.filename}`,
            image_url: imageUrl // 使用局域网可访问的URL
        });

    } catch (error) {
        console.error('❌ 上传错误:', error);
        res.status(500).json({
            success: false,
            error: error.message
        });
    }
});

// ✅ 获取图片列表接口
app.get('/api/uploads', (req, res) => {
    try {
        const files = fs.readdirSync(uploadsDir);
        const imageFiles = files.filter(file => {
            const ext = path.extname(file).toLowerCase();
            return ['.jpg', '.jpeg', '.png', '.gif', '.webp'].includes(ext);
        });

        const images = imageFiles.map(file => ({
            filename: file,
            url: `http://${LOCAL_IP}:${PORT}/uploads/${file}` // ✅ 修复：使用局域网IP
        }));

        console.log(`📁 找到 ${images.length} 个图片文件:`, images.map(img => img.filename));
        res.json(images);
    } catch (error) {
        console.error('❌ 读取图片列表错误:', error);
        res.status(500).json({ error: error.message });
    }
});

// 启动服务器
const PORT = process.env.PORT || 5000;

app.listen(PORT, async () => {
    console.log(`🚀 服务器运行在 http://localhost:${PORT}`);
    console.log(`🌐 局域网访问: http://${LOCAL_IP}:${PORT}`);
    console.log(`📁 静态文件服务: http://${LOCAL_IP}:${PORT}/uploads/`);
    console.log(`📤 文件上传接口: http://${LOCAL_IP}:${PORT}/api/upload`);
    console.log(`📋 图片列表接口: http://${LOCAL_IP}:${PORT}/api/uploads`);
    
    // 数据库连接测试
    try {
        const connection = await pool.getConnection();
        console.log('✅ 数据库连接成功！');
        connection.release();
    } catch (error) {
        console.error('❌ 数据库连接失败:', error.message);
    }
});
