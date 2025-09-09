const express = require('express');
const cors = require('cors');
const fs = require('fs').promises;
const path = require('path');

const app = express();
const PORT = 5000; // 使用5000端口
const HOST = '192.168.0.104';

// 用户数据文件路径
const USERS_FILE = path.join(__dirname, 'users.json');

// 中间件
app.use(cors());
app.use(express.json());

// 请求日志中间件
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
    console.log('Request body:', req.body);
    next();
});

// 初始化用户数据文件
async function initializeUsersFile() {
    try {
        await fs.access(USERS_FILE);
        console.log('用户数据文件已存在');
    } catch (error) {
        // 文件不存在，创建初始文件
        const initialUsers = [];
        await fs.writeFile(USERS_FILE, JSON.stringify(initialUsers, null, 2));
        console.log('创建用户数据文件');
    }
}

// 读取用户数据
async function readUsers() {
    try {
        const data = await fs.readFile(USERS_FILE, 'utf8');
        return JSON.parse(data);
    } catch (error) {
        console.error('读取用户数据失败:', error);
        return [];
    }
}

// 写入用户数据
async function writeUsers(users) {
    try {
        await fs.writeFile(USERS_FILE, JSON.stringify(users, null, 2));
        return true;
    } catch (error) {
        console.error('写入用户数据失败:', error);
        return false;
    }
}

// 生成用户ID - 修改为随机数字ID（字符串格式）
function generateUserId() {
    // 生成1-100之间的随机数字作为user_id，返回字符串格式
    return (Math.floor(Math.random() * 100) + 1).toString();
}

// 生成当前时间的ISO格式
function getCurrentTimeISO() {
    return new Date().toISOString();
}

// API路由

// 测试连接
app.get('/api/test', (req, res) => {
    console.log('收到测试连接请求');
    res.json({
        message: ' NeighborLink API is running!',
        port: PORT
    });
});

// 用户注册 - 按照新要求修改
app.post('/api/users', async (req, res) => {
    console.log('收到注册请求:', req.body);
    
    try {
        const { username, email, password, phone, gender, distance } = req.body;
        
        // 验证必填字段
        if (!username || !email || !password) {
            return res.status(400).json({
                success: false,
                message: '用户名、邮箱和密码为必填项'
            });
        }
        
        // 验证邮箱格式
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            return res.status(400).json({
                success: false,
                message: '邮箱格式不正确'
            });
        }
        
        // 读取现有用户
        const users = await readUsers();
        
        // 检查用户名是否已存在
        const existingUserByUsername = users.find(user => user.username === username);
        if (existingUserByUsername) {
            return res.status(400).json({
                success: false,
                message: '用户名已存在'
            });
        }
        
        // 检查邮箱是否已存在
        const existingUserByEmail = users.find(user => user.email === email);
        if (existingUserByEmail) {
            return res.status(400).json({
                success: false,
                message: '邮箱已被注册'
            });
        }
        
        // 生成新的user_id
        let newUserId;
        let isUnique = false;
        
        // 确保user_id唯一性
        while (!isUnique) {
            newUserId = generateUserId();
            const existingUser = users.find(user => user.user_id === newUserId);
            if (!existingUser) {
                isUnique = true;
            }
        }
        
        // 创建新用户 - 按照新要求
        const newUser = {
            user_id: newUserId,
            username,
            email,
            password, // 注意：实际项目中应该加密密码
            phone: phone || '',
            gender: gender || '',
            distance: distance || '0',
            created_at: getCurrentTimeISO(), // 使用ISO格式的当前时间
            avatar_url: `https://i.pravatar.cc/150?img=${Math.floor(Math.random() * 100) + 1}` // 使用1-100之间的随机数字
        };
        
        // 添加到用户列表
        users.push(newUser);
        
        // 保存到文件
        const saved = await writeUsers(users);
        if (!saved) {
            return res.status(500).json({
                success: false,
                message: '保存用户数据失败'
            });
        }
        
        // 返回成功响应（不包含密码）
        const { password: _, ...userWithoutPassword } = newUser;
        
        console.log('用户注册成功:', userWithoutPassword);
        res.status(201).json({
            success: true,
            message: '用户添加成功',
            user_id: newUserId,
            user: userWithoutPassword
        });
        
    } catch (error) {
        console.error('注册过程中发生错误:', error);
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 用户登录
app.post('/api/login', async (req, res) => {
    console.log('收到登录请求:', req.body);
    
    try {
        const { identifier, password } = req.body;
        
        if (!identifier || !password) {
            return res.status(400).json({
                success: false,
                message: '用户名/邮箱和密码为必填项'
            });
        }
        
        // 读取用户数据
        const users = await readUsers();
        
        // 查找用户（通过用户名或邮箱）
        const user = users.find(u => 
            u.username === identifier || u.email === identifier
        );
        
        if (!user) {
            return res.status(401).json({
                success: false,
                message: '用户不存在'
            });
        }
        
        // 验证密码
        if (user.password !== password) {
            return res.status(401).json({
                success: false,
                message: '密码错误'
            });
        }
        
        // 返回成功响应（不包含密码）
        const { password: _, ...userWithoutPassword } = user;
        
        console.log('用户登录成功:', userWithoutPassword);
        res.json({
            success: true,
            message: '登录成功',
            user: userWithoutPassword
        });
        
    } catch (error) {
        console.error('登录过程中发生错误:', error);
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 获取所有用户
app.get('/api/users', async (req, res) => {
    console.log('收到获取用户列表请求');
    
    try {
        const users = await readUsers();
        
        // 移除密码字段
        const usersWithoutPasswords = users.map(user => {
            const { password, ...userWithoutPassword } = user;
            return userWithoutPassword;
        });
        
        console.log(`返回 ${usersWithoutPasswords.length} 个用户`);
        res.json(usersWithoutPasswords);
        
    } catch (error) {
        console.error('获取用户列表失败:', error);
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 根据ID获取用户
app.get('/api/users/:id', async (req, res) => {
    const userId = parseInt(req.params.id);
    console.log(`收到获取用户请求，ID: ${userId}`);
    
    try {
        const users = await readUsers();
        const user = users.find(u => u.user_id === userId);
        
        if (!user) {
            return res.status(404).json({
                success: false,
                message: '用户不存在'
            });
        }
        
        // 移除密码字段
        const { password, ...userWithoutPassword } = user;
        
        console.log('找到用户:', userWithoutPassword);
        res.json(userWithoutPassword);
        
    } catch (error) {
        console.error('获取用户失败:', error);
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 更新用户信息
app.put('/api/users/:id', async (req, res) => {
    const userId = parseInt(req.params.id);
    console.log(`收到更新用户请求，ID: ${userId}`, req.body);
    
    try {
        const users = await readUsers();
        const userIndex = users.findIndex(u => u.user_id === userId);
        
        if (userIndex === -1) {
            return res.status(404).json({
                success: false,
                message: '用户不存在'
            });
        }
        
        // 更新用户信息
        const updatedUser = {
            ...users[userIndex],
            ...req.body,
            user_id: userId, // 确保ID不被修改
            updated_at: getCurrentTimeISO() // 添加更新时间
        };
        
        users[userIndex] = updatedUser;
        
        // 保存到文件
        const saved = await writeUsers(users);
        if (!saved) {
            return res.status(500).json({
                success: false,
                message: '保存用户数据失败'
            });
        }
        
        // 返回成功响应（不包含密码）
        const { password, ...userWithoutPassword } = updatedUser;
        
        console.log('用户更新成功:', userWithoutPassword);
        res.json({
            success: true,
            message: '用户信息更新成功',
            user: userWithoutPassword
        });
        
    } catch (error) {
        console.error('更新用户失败:', error);
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 删除用户
app.delete('/api/users/:id', async (req, res) => {
    const userId = parseInt(req.params.id);
    console.log(`收到删除用户请求，ID: ${userId}`);
    
    try {
        const users = await readUsers();
        const userIndex = users.findIndex(u => u.user_id === userId);
        
        if (userIndex === -1) {
            return res.status(404).json({
                success: false,
                message: '用户不存在'
            });
        }
        
        // 删除用户
        const deletedUser = users.splice(userIndex, 1)[0];
        
        // 保存到文件
        const saved = await writeUsers(users);
        if (!saved) {
            return res.status(500).json({
                success: false,
                message: '保存用户数据失败'
            });
        }
        
        console.log('用户删除成功:', deletedUser.username);
        res.json({
            success: true,
            message: '用户删除成功'
        });
        
    } catch (error) {
        console.error('删除用户失败:', error);
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 启动服务器
async function startServer() {
    await initializeUsersFile();
    
    app.listen(PORT, HOST, () => {
        console.log(` NeighborLink API服务器已启动`);
        console.log(` 服务器地址: http://${HOST}:${PORT}`);
        console.log(` 启动时间: ${getCurrentTimeISO()}`);
        console.log(' 可用端点:');
        console.log('   GET  /api/test - 测试连接');
        console.log('   POST /api/users - 用户注册');
        console.log('   POST /api/login - 用户登录');
        console.log('   GET  /api/users - 获取所有用户');
        console.log('   GET  /api/users/:id - 获取特定用户');
        console.log('   PUT  /api/users/:id - 更新用户信息');
        console.log('   DELETE /api/users/:id - 删除用户');
    });
}

startServer().catch(error => {
    console.error('启动服务器失败:', error);
});
