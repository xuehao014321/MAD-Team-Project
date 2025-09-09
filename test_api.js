const axios = require("axios");

const API_BASE_URL = "http://192.168.0.103:5000";

async function testAPI() {
    console.log(" ???? NeighborLink API...\n");

    try {
        // ??API??
        console.log("1 ??API??...");
        const testResponse = await axios.get(`${API_BASE_URL}/api/test`);
        console.log(" API????:", testResponse.data);
        console.log("");

        // ????????
        console.log("2 ????????...");
        const usersResponse = await axios.get(`${API_BASE_URL}/api/users`);
        console.log(" ????????!");
        console.log(" ????:", usersResponse.data.length);
        
        if (usersResponse.data.length > 0) {
            console.log(" ????:");
            usersResponse.data.forEach((user, index) => {
                console.log(`   ${index + 1}. ID: ${user.user_id}, ???: ${user.username}, ??: ${user.email}`);
            });
        } else {
            console.log("     ?????");
        }
        console.log("");

        // ????????
        console.log("3 ????????...");
        const itemsResponse = await axios.get(`${API_BASE_URL}/api/items`);
        console.log(" ????????!");
        console.log(" ????:", itemsResponse.data.length);
        
        if (itemsResponse.data.length > 0) {
            console.log("  ????:");
            itemsResponse.data.forEach((item, index) => {
                console.log(`   ${index + 1}. ID: ${item.item_id}, ??: ${item.title}, ??: ${item.price}`);
            });
        } else {
            console.log("     ?????");
        }

        console.log("\n ??????!");

    } catch (error) {
        console.error(" ????:", error.message);
        
        if (error.response) {
            console.error(" ????:", error.response.status);
            console.error(" ????:", error.response.data);
        } else if (error.request) {
            console.error(" ????: ?????API???");
            console.error(" ???:");
            console.error("   1. API?????????");
            console.error("   2. IP??????: 192.168.0.103");
            console.error("   3. ??????: 5000");
            console.error("   4. ????????");
        }
    }
}

testAPI();
