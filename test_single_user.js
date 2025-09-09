// 简单测试 - 添加单个用户
// 运行方式: node test_single_user.js

const API_BASE_URL = 'http://localhost:5000';

// 你可以修改这里的用户数据进行测试
const newUser = {
    username: 'john_doe',
    email: 'john@example.com',
    password: 'john123',
    phone: '13812345678',
    gender: 'male'
};

async function addSingleUser() {
    try {
        console.log('🚀 正在添加用户...');
        console.log('用户数据:', newUser);
        
        const response = await fetch(`${API_BASE_URL}/api/users`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(newUser)
        });

        const result = await response.json();
        
        console.log('\n📋 服务器响应:');
        console.log(JSON.stringify(result, null, 2));
        
        if (response.ok) {
            console.log('\n✅ 用户添加成功!');
            console.log('用户ID:', result.user.user_id);
            console.log('用户名:', result.user.username);
        } else {
            console.log('\n❌ 添加失败:', result.message);
        }
        
    } catch (error) {
        console.log('❌ 请求出错:', error.message);
        console.log('💡 请确保服务器正在运行 (python server.py)');
    }
}

// 运行测试
addSingleUser(); 