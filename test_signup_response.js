// 测试服务器注册响应
const API_BASE_URL = "http://192.168.0.104:5000/api";

async function testSignUpResponse() {
    try {
        console.log(" 测试注册响应格式...");
        
        const testUser = {
            username: `test_${Date.now()}`,
            email: `test_${Date.now()}@example.com`,
            password: "test123",
            phone: "010-1234567",
            gender: "Male",
            distance: "2.0"
        };
        
        console.log(" 发送注册请求:", testUser);
        
        const response = await fetch(`${API_BASE_URL}/users`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(testUser)
        });
        
        console.log(" 响应状态:", response.status);
        console.log(" 响应头:", Object.fromEntries(response.headers.entries()));
        
        const responseText = await response.text();
        console.log(" 原始响应文本:", responseText);
        
        try {
            const result = JSON.parse(responseText);
            console.log(" 解析后的响应:", JSON.stringify(result, null, 2));
            
            // 检查响应结构
            console.log("\n 响应结构分析:");
            console.log(" success:", result.success);
            console.log(" message:", result.message);
            console.log(" user_id:", result.user_id);
            console.log(" user:", result.user);
            
            if (result.user) {
                console.log("\n 用户信息详情:");
                console.log("  - id:", result.user.id);
                console.log("  - username:", result.user.username);
                console.log("  - email:", result.user.email);
                console.log("  - phone:", result.user.phone);
                console.log("  - gender:", result.user.gender);
                console.log("  - distance:", result.user.distance);
                console.log("  - avatar_url:", result.user.avatar_url);
                console.log("  - created_at:", result.user.created_at);
                console.log("  - password:", result.user.password ? "存在" : "不存在");
            } else {
                console.log(" 用户信息缺失!");
            }
            
        } catch (parseError) {
            console.error(" JSON解析失败:", parseError.message);
            console.log("原始响应:", responseText);
        }
        
    } catch (error) {
        console.error(" 测试失败:", error.message);
    }
}

testSignUpResponse();
