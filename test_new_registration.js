/**
 * 测试新的注册逻辑
 * 验证avatar_url和created_at格式
 */

const API_BASE_URL = 'http://192.168.0.104:5000/api';

console.log(' 测试新的注册逻辑...');
console.log(` API地址: ${API_BASE_URL}`);

async function testNewRegistrationLogic() {
    try {
        console.log('\n=== 测试新注册逻辑 ===');
        
        // 测试用户数据
        const testUser = {
            username: `newuser_${Date.now()}`,
            email: `new_${Date.now()}@test.com`,
            password: 'testpassword123',
            phone: '010-8888888',
            gender: 'Male',
            distance: '3.5'
        };
        
        console.log(' 注册新用户:', testUser.username);
        console.log(' 邮箱:', testUser.email);
        
        // 发送注册请求
        const response = await fetch(`${API_BASE_URL}/users`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(testUser)
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`注册失败: ${response.status}, ${errorText}`);
        }
        
        const result = await response.json();
        console.log(' 注册成功!');
        console.log(' 注册结果:');
        console.log('   - 用户ID:', result.user_id);
        console.log('   - 用户名:', result.user.username);
        console.log('   - 邮箱:', result.user.email);
        console.log('   - 头像URL:', result.user.avatar_url);
        console.log('   - 创建时间:', result.user.created_at);
        console.log('   - 性别:', result.user.gender);
        console.log('   - 距离:', result.user.distance);
        
        // 验证avatar_url格式
        const expectedAvatarPattern = `https://i.pravatar.cc/150?img=${result.user_id}`;
        if (result.user.avatar_url === expectedAvatarPattern) {
            console.log(' avatar_url格式正确!');
        } else {
            console.log(' avatar_url格式错误!');
            console.log('   期望:', expectedAvatarPattern);
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
        
        // 验证user_id是数字
        if (typeof result.user_id === 'number' && result.user_id > 0) {
            console.log(' user_id格式正确!');
        } else {
            console.log(' user_id格式错误!');
            console.log('   实际:', result.user_id, typeof result.user_id);
        }
        
        return result;
        
    } catch (error) {
        console.error(' 测试失败:', error.message);
        return null;
    }
}

async function testMultipleRegistrations() {
    console.log('\n=== 测试多次注册 ===');
    
    const promises = [];
    for (let i = 1; i <= 3; i++) {
        const testUser = {
            username: `multitest_${i}_${Date.now()}`,
            email: `multi_${i}_${Date.now()}@test.com`,
            password: 'test123',
            phone: `010-${i}${i}${i}${i}${i}${i}${i}`,
            gender: i % 2 === 0 ? 'Female' : 'Male',
            distance: (i * 1.5).toString()
        };
        
        promises.push(
            fetch(`${API_BASE_URL}/users`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(testUser)
            }).then(res => res.json())
        );
    }
    
    try {
        const results = await Promise.all(promises);
        console.log(' 批量注册成功!');
        
        results.forEach((result, index) => {
            console.log(`\n用户 ${index + 1}:`);
            console.log(`   ID: ${result.user_id}`);
            console.log(`   用户名: ${result.user.username}`);
            console.log(`   头像: ${result.user.avatar_url}`);
            console.log(`   时间: ${result.user.created_at}`);
        });
        
    } catch (error) {
        console.error(' 批量注册失败:', error.message);
    }
}

async function runAllTests() {
    console.log(' 开始测试新的注册逻辑...\n');
    
    // 测试单个注册
    await testNewRegistrationLogic();
    
    // 测试批量注册
    await testMultipleRegistrations();
    
    console.log('\n 所有测试完成!');
}

// 运行测试
runAllTests().catch(error => {
    console.error(' 测试过程中发生错误:', error);
});
