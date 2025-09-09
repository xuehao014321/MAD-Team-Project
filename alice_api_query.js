#!/usr/bin/env node

/**
 * Alice API Query Tool - 直接调用API查询Alice物品数量
 * 使用你应用中的API端点：http://192.168.0.103:5000/api
 */

const http = require('http');

// API配置 - 与你的Android应用中ApiClient相同
const API_BASE_URL = 'http://192.168.0.103:5000/api';
const ALICE_USER_ID = 1;  // Alice的用户ID是1

/**
 * 发送HTTP GET请求
 */
function httpGet(url) {
    return new Promise((resolve, reject) => {
        console.log('🌐 发送请求:', url);
        
        const req = http.get(url, (res) => {
            let data = '';
            
            res.on('data', (chunk) => {
                data += chunk;
            });
            
            res.on('end', () => {
                try {
                    if (res.statusCode === 200) {
                        const jsonData = JSON.parse(data);
                        resolve(jsonData);
                    } else {
                        reject(new Error(`HTTP ${res.statusCode}: ${data}`));
                    }
                } catch (error) {
                    reject(new Error(`JSON解析失败: ${error.message}`));
                }
            });
        });
        
        req.on('error', (error) => {
            reject(new Error(`网络请求失败: ${error.message}`));
        });
        
        req.setTimeout(10000, () => {
            req.destroy();
            reject(new Error('请求超时'));
        });
    });
}

/**
 * 获取所有用户列表
 */
async function getAllUsers() {
    console.log('📋 正在获取用户列表...');
    const users = await httpGet(`${API_BASE_URL}/users`);
    console.log(`✅ 成功获取 ${users.length} 个用户`);
    return users;
}

/**
 * 根据用户名查找用户
 */
async function getUserByUsername(username) {
    console.log(`🔍 正在查找用户: ${username}`);
    const users = await getAllUsers();
    
    const user = users.find(u => u.username === username);
    if (!user) {
        throw new Error(`用户 '${username}' 不存在`);
    }
    
    console.log(`✅ 找到用户: ${user.username} (ID: ${user.user_id})`);
    return user;
}

/**
 * 根据用户ID获取用户信息
 */
async function getUserById(userId) {
    console.log(`🔍 正在获取用户ID: ${userId}`);
    const user = await httpGet(`${API_BASE_URL}/users/${userId}`);
    console.log(`✅ 找到用户: ${user.username} (ID: ${user.user_id})`);
    return user;
}

/**
 * 获取用户的所有物品
 */
async function getItemsByUserId(userId) {
    console.log(`📦 正在获取用户 ${userId} 的物品列表...`);
    const items = await httpGet(`${API_BASE_URL}/items?user_id=${userId}`);
    console.log(`✅ 成功获取 ${items.length} 个物品`);
    return items;
}

/**
 * 主函数 - 查询Alice的物品数量 (用户ID=1)
 */
async function queryAliceItems() {
    console.log('🎯 === Alice物品数量查询工具 === 🎯\n');
    
    try {
        // 步骤1: 获取用户ID=1的用户信息（Alice）
        console.log('📍 步骤1: 获取Alice用户信息 (用户ID=1)');
        const aliceUser = await getUserById(ALICE_USER_ID);
        
        console.log('👤 Alice用户信息:');
        console.log(`   - 用户ID: ${aliceUser.user_id}`);
        console.log(`   - 用户名: ${aliceUser.username}`);
        console.log(`   - 邮箱: ${aliceUser.email}`);
        console.log(`   - 电话: ${aliceUser.phone}`);
        
        // 步骤2: 获取Alice的所有物品
        console.log('\n📍 步骤2: 获取Alice的物品列表');
        const aliceItems = await getItemsByUserId(ALICE_USER_ID);
        
        // 步骤3: 显示结果
        console.log('\n🎯🎯🎯 === 查询结果 === 🎯🎯🎯');
        console.log(`👤 用户: ${aliceUser.username} (ID: ${ALICE_USER_ID})`);
        console.log(`📦 物品总数: ${aliceItems.length} 个`);
        
        if (aliceItems.length > 0) {
            console.log('\n📋 物品详情:');
            aliceItems.forEach((item, index) => {
                console.log(`   ${index + 1}. ${item.name} (${item.status})`);
                console.log(`      - 描述: ${item.description}`);
                console.log(`      - 价格: $${item.price}`);
                console.log('');
            });
        }
        
        console.log('=====================================');
        console.log(`🎉 Alice用户共有 ${aliceItems.length} 个物品`);
        console.log('=====================================\n');
        
        return aliceItems.length;
        
    } catch (error) {
        console.error('\n❌ === 查询失败 === ❌');
        console.error('⚠️ 错误信息:', error.message);
        
        if (error.message.includes('ECONNREFUSED')) {
            console.error('💡 建议: 请确保API服务器正在运行 (http://192.168.0.103:5000)');
        } else if (error.message.includes('网络请求失败')) {
            console.error('💡 建议: 请检查网络连接和API服务器地址');
        }
        
        console.error('=====================================\n');
        throw error;
    }
}

/**
 * 测试API连接
 */
async function testApiConnection() {
    console.log('🔧 正在测试API连接...');
    
    try {
        await httpGet(`${API_BASE_URL.replace('/api', '')}/api/test`);
        console.log('✅ API连接正常');
        return true;
    } catch (error) {
        console.log('❌ API连接失败:', error.message);
        return false;
    }
}

// 如果直接运行此脚本
if (require.main === module) {
    (async () => {
        try {
            // 首先测试API连接
            const isConnected = await testApiConnection();
            if (!isConnected) {
                console.log('\n⚠️ 无法连接到API服务器，尝试直接查询...\n');
            }
            
            // 执行Alice物品查询
            const itemCount = await queryAliceItems();
            process.exit(0);
            
        } catch (error) {
            console.error('脚本执行失败:', error.message);
            process.exit(1);
        }
    })();
}

module.exports = { queryAliceItems, testApiConnection }; 