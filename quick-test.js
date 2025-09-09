// 快速测试脚本 - 专门测试API数据读取功能
const axios = require('axios');

// API配置
const API_BASE_URL = 'http://192.168.0.103:5000';
const LOCAL_API_URL = 'http://localhost:5000';

// 简单的日志函数
const log = {
    success: (msg) => console.log(`✅ ${msg}`),
    error: (msg) => console.log(`❌ ${msg}`),
    info: (msg) => console.log(`ℹ️ ${msg}`)
};

async function quickTest() {
    console.log('🚀 快速测试 NeighborLink API 数据读取功能...\n');
    
    // 选择API URL
    let baseURL = API_BASE_URL;
    try {
        const testResponse = await axios.get(`${API_BASE_URL}/api/test`, { timeout: 3000 });
        log.info(`连接成功: ${API_BASE_URL}`);
        console.log(`服务器信息:`, testResponse.data);
    } catch (error) {
        log.info('局域网IP连接失败，尝试localhost...');
        baseURL = LOCAL_API_URL;
        try {
            const testResponse = await axios.get(`${LOCAL_API_URL}/api/test`, { timeout: 3000 });
            log.info(`连接成功: ${LOCAL_API_URL}`);
            console.log(`服务器信息:`, testResponse.data);
        } catch (err) {
            log.error('无法连接到API服务器！请确认服务器已启动');
            return;
        }
    }
    
    console.log('\n📊 开始测试数据读取...');
    
    // 测试用户数据读取
    try {
        const usersResponse = await axios.get(`${baseURL}/api/users`, { timeout: 5000 });
        log.success(`用户数据读取成功 - 共 ${usersResponse.data.length} 个用户`);
        
        if (usersResponse.data.length > 0) {
            console.log('用户示例数据:');
            console.log(JSON.stringify(usersResponse.data[0], null, 2));
        }
    } catch (error) {
        log.error(`用户数据读取失败: ${error.message}`);
        if (error.response) {
            console.log(`错误详情: ${error.response.status} - ${JSON.stringify(error.response.data)}`);
        }
    }
    
    console.log('');
    
    // 测试物品数据读取
    try {
        const itemsResponse = await axios.get(`${baseURL}/api/items`, { timeout: 5000 });
        log.success(`物品数据读取成功 - 共 ${itemsResponse.data.length} 个物品`);
        
        if (itemsResponse.data.length > 0) {
            console.log('物品示例数据:');
            console.log(JSON.stringify(itemsResponse.data[0], null, 2));
        }
    } catch (error) {
        log.error(`物品数据读取失败: ${error.message}`);
        if (error.response) {
            console.log(`错误详情: ${error.response.status} - ${JSON.stringify(error.response.data)}`);
        }
    }
    
    console.log('');
    
    // 测试上传文件列表读取
    try {
        const uploadsResponse = await axios.get(`${baseURL}/api/uploads`, { timeout: 5000 });
        log.success(`上传文件读取成功 - 共 ${uploadsResponse.data.count} 个文件`);
        
        if (uploadsResponse.data.files.length > 0) {
            console.log('文件示例:');
            console.log(JSON.stringify(uploadsResponse.data.files.slice(0, 3), null, 2));
        }
    } catch (error) {
        log.error(`上传文件读取失败: ${error.message}`);
        if (error.response) {
            console.log(`错误详情: ${error.response.status} - ${JSON.stringify(error.response.data)}`);
        }
    }
    
    console.log('\n🎉 快速测试完成！');
    
    // 显示总结
    console.log('\n📋 测试总结:');
    console.log('- API服务器连接状态: ✅');
    console.log('- 数据库连接状态: 需要查看上面的测试结果');
    console.log('- 数据读取功能: 需要查看上面的测试结果');
}

// 运行测试
if (require.main === module) {
    quickTest().catch(error => {
        console.error('测试过程中发生错误:', error.message);
    });
}

module.exports = quickTest; 