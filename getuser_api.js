/**
 * 用户查询API - 简单的用户数据获取功能
 */

// 远程API地址
const API_BASE_URL = 'http://192.168.0.104:5000/api';

/**
 * 获取所有用户列表
 * @returns {Promise<Array>} 用户数组
 */
async function getAllUsers() {
    try {
        const response = await fetch(`${API_BASE_URL}/users`);
        
        if (!response.ok) {
            throw new Error(`获取用户失败: ${response.status}`);
        }
        
        const users = await response.json();
        console.log('获取到用户数据:', users);
        return users;
        
    } catch (error) {
        console.error('获取用户列表失败:', error);
        return [];
    }
}

/**
 * 根据用户ID获取特定用户
 * @param {number} userId - 用户ID
 * @returns {Promise<Object|null>} 用户对象或null
 */
async function getUserById(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/users/${userId}`);
        
        if (!response.ok) {
            if (response.status === 404) {
                console.log(`用户ID ${userId} 不存在`);
                return null;
            }
            throw new Error(`获取用户失败: ${response.status}`);
        }
        
        const user = await response.json();
        console.log('获取到用户:', user);
        return user;
        
    } catch (error) {
        console.error(`获取用户ID ${userId} 失败:`, error);
        return null;
    }
}

/**
 * 根据用户名搜索用户
 * @param {string} username - 用户名
 * @returns {Promise<Array>} 匹配的用户数组
 */
async function searchUsersByName(username) {
    try {
        const allUsers = await getAllUsers();
        const matchedUsers = allUsers.filter(user => 
            user.username && user.username.toLowerCase().includes(username.toLowerCase())
        );
        
        console.log(`搜索用户名 "${username}" 的结果:`, matchedUsers);
        return matchedUsers;
        
    } catch (error) {
        console.error('搜索用户失败:', error);
        return [];
    }
}

/**
 * 显示用户信息
 * @param {Object} user - 用户对象
 */
function displayUser(user) {
    if (!user) {
        console.log('用户不存在');
        return;
    }
    
    console.log('用户信息:');
    console.log(`ID: ${user.user_id || user.id}`);
    console.log(`用户名: ${user.username}`);
    console.log(`邮箱: ${user.email}`);
    console.log(`电话: ${user.phone || '未提供'}`);
    console.log(`性别: ${user.gender || '未提供'}`);
    console.log(`距离: ${user.distance || 0}km`);
    console.log(`注册时间: ${user.created_at || '未知'}`);
}

/**
 * 显示用户列表
 * @param {Array} users - 用户数组
 */
function displayUserList(users) {
    if (!users || users.length === 0) {
        console.log('没有找到用户');
        return;
    }
    
    console.log(`找到 ${users.length} 个用户:`);
    users.forEach((user, index) => {
        console.log(`${index + 1}. ${user.username} (ID: ${user.user_id || user.id})`);
    });
}

// 使用示例函数
async function testQueries() {
    console.log('开始测试查询功能...');
    
    // 1. 获取所有用户
    console.log('\n=== 获取所有用户 ===');
    const allUsers = await getAllUsers();
    displayUserList(allUsers);
    
    // 2. 获取特定用户
    if (allUsers.length > 0) {
        console.log('\n=== 获取第一个用户详情 ===');
        const firstUserId = allUsers[0].user_id || allUsers[0].id;
        const user = await getUserById(firstUserId);
        displayUser(user);
    }
    
    // 3. 搜索用户
    console.log('\n=== 搜索用户 ===');
    const searchResults = await searchUsersByName('test');
    displayUserList(searchResults);
}

// 直接运行测试
testQueries().catch(console.error);
