// 修复服务器响应格式
const fs = require("fs");

// 读取当前server.js文件
const serverContent = fs.readFileSync("server.js", "utf8");

// 查找并替换注册响应部分
const oldResponse = `        // 返回成功响应（不包含密码）
        const { password: _, ...userWithoutPassword } = newUser;
        
        console.log('用户注册成功:', userWithoutPassword);
        res.status(201).json({
            success: true,
            message: '用户添加成功',
            user_id: newUserId,
            user: userWithoutPassword
        });`;

const newResponse = `        // 返回成功响应（不包含密码）
        const { password: _, ...userWithoutPassword } = newUser;
        
        console.log('用户注册成功:', userWithoutPassword);
        console.log('返回完整响应:', {
            success: true,
            message: '用户添加成功',
            user_id: newUserId,
            user: userWithoutPassword
        });
        
        res.status(201).json({
            success: true,
            message: '用户添加成功',
            user_id: newUserId,
            user: userWithoutPassword
        });`;

// 替换内容
const newServerContent = serverContent.replace(oldResponse, newResponse);

// 写回文件
fs.writeFileSync("server.js", newServerContent, "utf8");

console.log(" 服务器响应格式已修复，添加了调试日志");
