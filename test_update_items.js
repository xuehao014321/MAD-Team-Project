const axios = require('axios');

// 配置
const API_BASE_URL = 'http://192.168.0.103:5000';

// 测试数据
const testUpdates = [
    {
        itemId: 1,
        updates: {
            title: '测试修改标题 - 更新版本',
            description: '这是一个测试描述，用于验证PATCH接口是否正常工作',
            price: '99.99',
            status: 'Sold'
        }
    },
    {
        itemId: 2,
        updates: {
            title: '另一个测试商品',
            description: '更新后的商品描述',
            price: '199.50',
            views: 100,
            likes: 25
        }
    }
];

// 测试函数
async function testUpdateItems() {
    console.log('🚀 开始测试items数据更新...\n');

    try {
        // 1. 首先获取所有items，查看当前数据
        console.log('📋 步骤1: 获取当前所有items数据');
        const getItemsResponse = await axios.get(`${API_BASE_URL}/api/items`);
        console.log(`✅ 成功获取 ${getItemsResponse.data.length} 个items`);
        
        if (getItemsResponse.data.length > 0) {
            console.log('📦 当前items列表:');
            getItemsResponse.data.forEach((item, index) => {
                console.log(`  ${index + 1}. ID: ${item.item_id}, 标题: ${item.title}, 价格: ${item.price}, 状态: ${item.status}`);
            });
        }
        console.log('');

        // 2. 测试更新第一个item
        if (getItemsResponse.data.length > 0) {
            const firstItem = getItemsResponse.data[0];
            const itemId = firstItem.item_id;
            
            console.log(`📝 步骤2: 更新item ID ${itemId}`);
            console.log(`   原始数据: 标题="${firstItem.title}", 价格="${firstItem.price}", 状态="${firstItem.status}"`);
            
            const updateData = {
                title: `更新后的标题 - ${new Date().toLocaleTimeString()}`,
                description: `这是更新后的描述，修改时间: ${new Date().toLocaleString()}`,
                price: (Math.random() * 1000 + 10).toFixed(2),
                status: 'Available',
                views: Math.floor(Math.random() * 100),
                likes: Math.floor(Math.random() * 50)
            };
            
            console.log(`   更新数据:`, updateData);
            
            try {
                const updateResponse = await axios.patch(`${API_BASE_URL}/api/items/${itemId}`, updateData);
                console.log(`✅ 更新成功:`, updateResponse.data);
            } catch (updateError) {
                console.log(`❌ 更新失败:`, updateError.response?.data || updateError.message);
            }
        }
        console.log('');

        // 3. 验证更新结果
        console.log('🔍 步骤3: 验证更新结果');
        const verifyResponse = await axios.get(`${API_BASE_URL}/api/items`);
        console.log(`✅ 验证完成，当前有 ${verifyResponse.data.length} 个items`);
        
        if (verifyResponse.data.length > 0) {
            const updatedItem = verifyResponse.data[0];
            console.log(`📦 更新后的第一个item:`);
            console.log(`   ID: ${updatedItem.item_id}`);
            console.log(`   标题: ${updatedItem.title}`);
            console.log(`   描述: ${updatedItem.description}`);
            console.log(`   价格: ${updatedItem.price}`);
            console.log(`   状态: ${updatedItem.status}`);
            console.log(`   浏览数: ${updatedItem.views}`);
            console.log(`   点赞数: ${updatedItem.likes}`);
        }
        console.log('');

        // 4. 测试批量更新多个字段
        console.log('🔄 步骤4: 测试批量更新多个字段');
        if (getItemsResponse.data.length > 1) {
            const secondItem = getItemsResponse.data[1];
            const itemId = secondItem.item_id;
            
            const batchUpdateData = {
                title: `批量更新标题 - ${Date.now()}`,
                price: '888.88',
                status: 'Reserved',
                views: 999,
                likes: 88
            };
            
            console.log(`   更新item ID ${itemId} 的多个字段:`, batchUpdateData);
            
            try {
                const batchUpdateResponse = await axios.patch(`${API_BASE_URL}/api/items/${itemId}`, batchUpdateData);
                console.log(`✅ 批量更新成功:`, batchUpdateResponse.data);
            } catch (batchError) {
                console.log(`❌ 批量更新失败:`, batchError.response?.data || batchError.message);
            }
        }
        console.log('');

        // 5. 测试错误情况
        console.log('⚠️  步骤5: 测试错误情况');
        
        // 测试更新不存在的item
        try {
            const nonExistentResponse = await axios.patch(`${API_BASE_URL}/api/items/99999`, { title: '不存在的item' });
            console.log('❌ 意外成功更新了不存在的item');
        } catch (nonExistentError) {
            console.log('✅ 正确拒绝了不存在的item更新:', nonExistentError.response?.data?.error || 'Item not found');
        }
        
        // 测试空更新
        try {
            const emptyUpdateResponse = await axios.patch(`${API_BASE_URL}/api/items/1`, {});
            console.log('❌ 意外接受了空更新');
        } catch (emptyUpdateError) {
            console.log('✅ 正确拒绝了空更新:', emptyUpdateError.response?.data?.error || 'No fields provided');
        }
        console.log('');

        console.log('🎉 测试完成！');

    } catch (error) {
        console.error('❌ 测试过程中发生错误:', error.message);
        if (error.response) {
            console.error('   响应数据:', error.response.data);
        }
    }
}

// 运行测试
testUpdateItems();
