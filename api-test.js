// NeighborLink API 测试文件
// 用于测试所有API接口是否正常工作

const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const path = require('path');

// API配置
const API_BASE_URL = 'http://192.168.0.103:5000';
const LOCAL_API_URL = 'http://localhost:5000';

// 颜色输出函数
const colors = {
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    reset: '\x1b[0m'
};

const log = {
    success: (msg) => console.log(`${colors.green}✅ ${msg}${colors.reset}`),
    error: (msg) => console.log(`${colors.red}❌ ${msg}${colors.reset}`),
    warning: (msg) => console.log(`${colors.yellow}⚠️ ${msg}${colors.reset}`),
    info: (msg) => console.log(`${colors.blue}ℹ️ ${msg}${colors.reset}`)
};

// 测试函数
async function testAPI() {
    console.log('🚀 开始测试 NeighborLink API...\n');
    
    // 选择API URL（优先使用局域网IP）
    let baseURL = API_BASE_URL;
    try {
        await axios.get(`${API_BASE_URL}/api/test`, { timeout: 3000 });
        log.info(`使用局域网IP: ${API_BASE_URL}`);
    } catch (error) {
        log.warning('局域网IP连接失败，尝试使用localhost');
        baseURL = LOCAL_API_URL;
        try {
            await axios.get(`${LOCAL_API_URL}/api/test`, { timeout: 3000 });
            log.info(`使用localhost: ${LOCAL_API_URL}`);
        } catch (err) {
            log.error('无法连接到API服务器！');
            return;
        }
    }

    // 1. 测试根路径
    await testEndpoint('GET', '/', '根路径');

    // 2. 测试API状态
    await testEndpoint('GET', '/api/test', 'API状态测试');

    // 3. 测试获取所有用户
    await testEndpoint('GET', '/api/users', '获取所有用户');

    // 4. 测试获取所有物品
    await testEndpoint('GET', '/api/items', '获取所有物品');

    // 5. 测试获取特定用户的物品
    await testEndpoint('GET', '/api/users/1/items', '获取用户ID=1的物品');

    // 6. 测试获取上传文件列表
    await testEndpoint('GET', '/api/uploads', '获取上传文件列表');

    // 7. 测试添加新商品
    await testAddItem();

    // 8. 测试更新用户信息
    await testUpdateUser();

    // 9. 测试更新物品信息
    await testUpdateItem();

    console.log('\n🎉 API测试完成！');

    // 测试单个接口的通用函数
    async function testEndpoint(method, endpoint, description) {
        try {
            const url = `${baseURL}${endpoint}`;
            const response = await axios({
                method: method.toLowerCase(),
                url: url,
                timeout: 5000
            });
            
            log.success(`${description} - 状态码: ${response.status}`);
            console.log(`   响应数据:`, JSON.stringify(response.data, null, 2).substring(0, 200) + '...\n');
            return response.data;
        } catch (error) {
            log.error(`${description} - 失败: ${error.message}`);
            if (error.response) {
                console.log(`   错误详情: ${error.response.status} - ${JSON.stringify(error.response.data)}\n`);
            } else {
                console.log(`   网络错误: ${error.message}\n`);
            }
            return null;
        }
    }

    // 测试添加新商品
    async function testAddItem() {
        try {
            const newItem = {
                user_id: 1,
                title: '测试商品',
                description: '这是一个测试商品的描述',
                price: 99.99,
                image_url: `${baseURL}/uploads/test-image.jpg`,
                status: 'Available',
                views: 0,
                likes: 0,
                distance: 1.5
            };

            const response = await axios.post(`${baseURL}/api/items`, newItem, {
                headers: {
                    'Content-Type': 'application/json'
                },
                timeout: 5000
            });

            log.success(`添加新商品 - 状态码: ${response.status}`);
            console.log(`   响应数据:`, JSON.stringify(response.data, null, 2));
            console.log();
            return response.data;
        } catch (error) {
            log.error(`添加新商品 - 失败: ${error.message}`);
            if (error.response) {
                console.log(`   错误详情: ${error.response.status} - ${JSON.stringify(error.response.data)}`);
            }
            console.log();
            return null;
        }
    }

    // 测试更新用户信息
    async function testUpdateUser() {
        try {
            const updateData = {
                email: 'test@example.com',
                phone: '1234567890'
            };

            const response = await axios.patch(`${baseURL}/api/users/1`, updateData, {
                headers: {
                    'Content-Type': 'application/json'
                },
                timeout: 5000
            });

            log.success(`更新用户信息 - 状态码: ${response.status}`);
            console.log(`   响应数据:`, JSON.stringify(response.data, null, 2));
            console.log();
            return response.data;
        } catch (error) {
            log.error(`更新用户信息 - 失败: ${error.message}`);
            if (error.response) {
                console.log(`   错误详情: ${error.response.status} - ${JSON.stringify(error.response.data)}`);
            }
            console.log();
            return null;
        }
    }

    // 测试更新物品信息
    async function testUpdateItem() {
        try {
            const updateData = {
                price: 88.88,
                status: 'Sold',
                views: 10
            };

            const response = await axios.patch(`${baseURL}/api/items/1`, updateData, {
                headers: {
                    'Content-Type': 'application/json'
                },
                timeout: 5000
            });

            log.success(`更新物品信息 - 状态码: ${response.status}`);
            console.log(`   响应数据:`, JSON.stringify(response.data, null, 2));
            console.log();
            return response.data;
        } catch (error) {
            log.error(`更新物品信息 - 失败: ${error.message}`);
            if (error.response) {
                console.log(`   错误详情: ${error.response.status} - ${JSON.stringify(error.response.data)}`);
            }
            console.log();
            return null;
        }
    }
}

// 文件上传测试函数
async function testFileUpload() {
    console.log('\n📤 开始测试文件上传...');
    
    // 创建测试图片文件
    const testImagePath = path.join(__dirname, 'test-image.png');
    const testImageData = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==', 'base64');
    
    try {
        // 写入测试图片
        fs.writeFileSync(testImagePath, testImageData);
        
        // 创建表单数据
        const formData = new FormData();
        formData.append('image', fs.createReadStream(testImagePath));
        
        // 选择API URL
        let baseURL = API_BASE_URL;
        try {
            await axios.get(`${API_BASE_URL}/api/test`, { timeout: 3000 });
        } catch (error) {
            baseURL = LOCAL_API_URL;
        }
        
        // 发送上传请求
        const response = await axios.post(`${baseURL}/api/upload`, formData, {
            headers: {
                ...formData.getHeaders()
            },
            timeout: 10000
        });
        
        log.success(`文件上传 - 状态码: ${response.status}`);
        console.log(`   响应数据:`, JSON.stringify(response.data, null, 2));
        
        // 清理测试文件
        fs.unlinkSync(testImagePath);
        
    } catch (error) {
        log.error(`文件上传 - 失败: ${error.message}`);
        if (error.response) {
            console.log(`   错误详情: ${error.response.status} - ${JSON.stringify(error.response.data)}`);
        }
        
        // 清理测试文件（如果存在）
        if (fs.existsSync(testImagePath)) {
            fs.unlinkSync(testImagePath);
        }
    }
}

// 数据库连接测试
async function testDatabaseConnection() {
    console.log('\n🗄️ 开始测试数据库连接...');
    
    try {
        let baseURL = API_BASE_URL;
        try {
            await axios.get(`${API_BASE_URL}/api/test`, { timeout: 3000 });
        } catch (error) {
            baseURL = LOCAL_API_URL;
        }
        
        // 测试用户表查询
        const usersResponse = await axios.get(`${baseURL}/api/users`, { timeout: 5000 });
        log.success(`用户表查询成功 - 找到 ${usersResponse.data.length} 个用户`);
        
        // 测试物品表查询
        const itemsResponse = await axios.get(`${baseURL}/api/items`, { timeout: 5000 });
        log.success(`物品表查询成功 - 找到 ${itemsResponse.data.length} 个物品`);
        
    } catch (error) {
        log.error(`数据库连接测试失败: ${error.message}`);
        if (error.response) {
            console.log(`   错误详情: ${error.response.status} - ${JSON.stringify(error.response.data)}`);
        }
    }
}

// 主函数
async function main() {
    console.log('🔍 NeighborLink API 完整测试工具');
    console.log('=====================================\n');
    
    // 检查依赖
    try {
        require('axios');
        require('form-data');
    } catch (error) {
        log.error('缺少依赖包！请运行: npm install axios form-data');
        return;
    }
    
    // 执行所有测试
    await testAPI();
    await testFileUpload();
    await testDatabaseConnection();
    
    console.log('\n✨ 所有测试完成！');
}

// 运行测试
if (require.main === module) {
    main().catch(console.error);
}

module.exports = {
    testAPI,
    testFileUpload,
    testDatabaseConnection
}; 