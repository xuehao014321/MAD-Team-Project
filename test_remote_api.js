/**
 * 远程API测试脚本
 * 测试连接到远程服务器的API功能
 */

const API_BASE_URL = 'http://192.168.0.104:5000/api';

console.log(' 开始测试远程API连接...');
console.log(` 远程API地址: ${API_BASE_URL}`);

/**
 * 测试API连接
 */
async function testAPIConnection() {
    try {
        console.log('\n=== 测试API连接 ===');
        const response = await fetch(`${API_BASE_URL.replace('/api', '')}/api/test`);
        
        if (!response.ok) {
            throw new Error(`连接失败: ${response.status}`);
        }
        
        const result = await response.json();
        console.log(' API连接成功!');
        console.log('服务器响应:', result);
        return true;
    } catch (error) {
        console.error(' API连接失败:', error.message);
        return false;
    }
}

/**
 * 获取所有用户
 */
async function getAllUsers() {
    try {
        console.log('\n=== 获取所有用户 ===');
        const response = await fetch(`${API_BASE_URL}/users`);
        
        if (!response.ok) {
            throw new Error(`获取用户失败: ${response.status}`);
        }
        
        const users = await response.json();
        console.log(` 成功获取 ${users.length} 个用户:`);
        
        users.forEach((user, index) => {
            console.log(`${index + 1}. ${user.username} (ID: ${user.user_id})`);
            console.log(`   邮箱: ${user.email}`);
            console.log(`   性别: ${user.gender}`);
            console.log(`   距离: ${user.distance}km`);
            console.log(`   头像: ${user.avatar_url}`);
            console.log('');
        });
        
        return users;
    } catch (error) {
        console.error(' 获取用户失败:', error.message);
        return [];
    }
}

/**
 * 测试用户注册
 */
async function testUserRegistration() {
    try {
        console.log('\n=== 测试用户注册 ===');
        
        const newUser = {
            username: `testuser_${Date.now()}`,
            email: `test_${Date.now()}@example.com`,
            password: 'testpassword123',
            phone: '010-1234567',
            gender: 'Male',
            distance: '2.5'
        };
        
        console.log(' 注册新用户:', newUser.username);
        
        const response = await fetch(`${API_BASE_URL}/users`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(newUser)
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`注册失败: ${response.status}, ${errorText}`);
        }
        
        const result = await response.json();
        console.log(' 用户注册成功!');
        console.log('注册结果:', result);
        
        return result;
    } catch (error) {
        console.error(' 用户注册失败:', error.message);
        return null;
    }
}

/**
 * 主测试函数
 */
async function runAllTests() {
    console.log(' 开始远程API全面测试...\n');
    
    // 1. 测试连接
    const connectionOk = await testAPIConnection();
    if (!connectionOk) {
        console.log('\n 无法连接到远程API，测试终止');
        return;
    }
    
    // 2. 获取所有用户
    const users = await getAllUsers();
    
    // 3. 测试用户注册
    await testUserRegistration();
    
    console.log('\n 远程API测试完成!');
    console.log(' 测试总结:');
    console.log('    API连接测试');
    console.log('    获取用户列表');
    console.log('    用户注册测试');
}

// 运行测试
runAllTests().catch(error => {
    console.error(' 测试过程中发生错误:', error);
});
