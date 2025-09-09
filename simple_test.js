/**
 * 简单测试新的注册逻辑
 */

const API_BASE_URL = 'http://192.168.0.104:5000/api';

async function simpleTest() {
    try {
        console.log(' 简单测试新注册逻辑...');
        
        const testUser = {
            username: `simple_${Date.now()}`,
            email: `simple_${Date.now()}@test.com`,
            password: 'test123',
            phone: '010-1234567',
            gender: 'Male',
            distance: '2.0'
        };
        
        console.log(' 注册用户:', testUser.username);
        
        const response = await fetch(`${API_BASE_URL}/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(testUser)
        });
        
        const result = await response.json();
        console.log(' 完整响应:', JSON.stringify(result, null, 2));
        
        if (result.success) {
            console.log(' 注册成功!');
            console.log(' 用户ID:', result.user_id);
            console.log(' 用户名:', result.user?.username || 'N/A');
            console.log(' 邮箱:', result.user?.email || 'N/A');
            console.log(' 头像URL:', result.user?.avatar_url || 'N/A');
            console.log(' 创建时间:', result.user?.created_at || 'N/A');
            
            // 验证格式
            if (result.user?.avatar_url) {
                const expectedPattern = `https://i.pravatar.cc/150?img=${result.user_id}`;
                if (result.user.avatar_url === expectedPattern) {
                    console.log(' avatar_url格式正确!');
                } else {
                    console.log(' avatar_url格式错误!');
                    console.log('   期望:', expectedPattern);
                    console.log('   实际:', result.user.avatar_url);
                }
            }
            
            if (result.user?.created_at) {
                const timeRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/;
                if (timeRegex.test(result.user.created_at)) {
                    console.log(' 时间格式正确!');
                } else {
                    console.log(' 时间格式错误!');
                }
            }
        } else {
            console.log(' 注册失败:', result.message);
        }
        
    } catch (error) {
        console.error(' 测试出错:', error.message);
    }
}

simpleTest();
