// 远程API配置
const API_BASE_URL = 'http://192.168.0.104:5000/api'; // 替换为您的实际API地址

// 获取用户数据的API调用函数
async function fetchUsers() {
  try {
    const response = await fetch(`${API_BASE_URL}/users`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const users = await response.json();
    return users;
  } catch (error) {
    console.error('获取用户数据失败:', error);
    return [];
  }
}

// 添加新用户的API调用函数
async function addUser(userData) {
  try {
    console.log('🔗 发送请求到:', `${API_BASE_URL}/users`);
    console.log('📝 用户数据:', userData);
    
    const response = await fetch(`${API_BASE_URL}/users`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData)
    });
    
    console.log('📊 响应状态:', response.status);
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('❌ 服务器错误响应:', errorText);
      throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
    }
    
    const newUser = await response.json();
    return newUser;
  } catch (error) {
    console.error('添加用户失败:', error.message);
    return null;
  }
}

// 导出API函数
module.exports = {
  fetchUsers,
  addUser,
  API_BASE_URL
}; 