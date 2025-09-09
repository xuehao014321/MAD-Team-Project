/**
 * 测试服务器连接和注册
 */

const API_BASE_URL = 'http://192.168.0.104:5000/api';

async function testConnection() {
    try {
        console.log(' 测试服务器连接...');
        const response = await fetch(`${API_BASE_URL.replace('/api', '')}/api/test`);
        const result = await response.json();
        console.log(' 连接成功:', result);
        return true;
    } catch (error) {
        console.error(' 连接失败:', error.message);
        return false;
    }
}

async function testRegistration() {
    try {
        console.log('\n 测试用户注册...');
        
        const testUser = {
            username: `debug_${Date.now()}`,
            email: `debug_${Date.now()}@test.com`,
            password: 'test123',
            phone: '010-9999999',
            gender: 'Female',
            distance: '1.5'
        };
        
        console.log(' 发送数据:', testUser);
        
        const response = await fetch(`${API_BASE_URL}/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(testUser)
        });
        
        console.log(' 响应状态:', response.status);
        console.log(' 响应头:', Object.fromEntries(response.headers.entries()));
        
        const result = await response.json();
        console.log(' 响应数据:', JSON.stringify(result, null, 2));
        
        if (result.success && result.user) {
            console.log('\n 注册成功!');
            console.log(' 用户ID:', result.user_id);
            console.log(' 用户名:', result.user.username);
            console.log(' 邮箱:', result.user.email);
            console.log(' 头像URL:', result.user.avatar_url);
            console.log(' 创建时间:', result.user.created_at);
            console.log(' 电话:', result.user.phone);
            console.log(' 性别:', result.user.gender);
            console.log(' 距离:', result.user.distance);
            
            // 验证格式
            console.log('\n 格式验证:');
            
            // 验证avatar_url
            const expectedAvatar = `https://i.pravatar.cc/150?img=${result.user_id}`;
            if (result.user.avatar_url === expectedAvatar) {
                console.log(' avatar_url格式正确!');
            } else {
                console.log(' avatar_url格式错误!');
                console.log('   期望:', expectedAvatar);
                console.log('   实际:', result.user.avatar_url);
            }
            
            // 验证时间格式
            const timeRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/;
            if (timeRegex.test(result.user.created_at)) {
                console.log(' 时间格式正确!');
            } else {
                console.log(' 时间格式错误!');
                console.log('   实际:', result.user.created_at);
            }
            
            // 验证user_id
            if (typeof result.user_id === 'number' && result.user_id > 0) {
                console.log(' user_id格式正确!');
            } else {
                console.log(' user_id格式错误!');
                console.log('   实际:', result.user_id, typeof result.user_id);
            }
            
        } else {
            console.log(' 注册失败或响应格式错误');
        }
        
    } catch (error) {
        console.error(' 注册测试出错:', error.message);
    }
}

async function runTest() {
    const connected = await testConnection();
    if (connected) {
        await testRegistration();
    }
}

runTest();
