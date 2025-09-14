const os = require('os');
const fs = require('fs');
const path = require('path');

// ✅ Precise WLAN interface matching
const getWLANIP = () => {
    const interfaces = os.networkInterfaces();
    const targetInterfaces = ['wlan', 'wi-fi', 'wireless', '无线'];
    
    for (const interfaceName in interfaces) {
        const lowerName = interfaceName.toLowerCase();
        
        if (targetInterfaces.some(target => lowerName.includes(target))) {
            const iface = interfaces[interfaceName];
            for (let i = 0; i < iface.length; i++) {
                const alias = iface[i];
                if (alias.family === 'IPv4' && !alias.internal) {
                    return alias.address;
                }
            }
        }
    }
    
    return 'localhost';
};

const LOCAL_IP = getWLANIP();
const PORT = process.env.PORT || 5000;
const NEW_API_URL = `http://${LOCAL_IP}:${PORT}/api`;

console.log(`Detected local WiFi IP: ${LOCAL_IP}`);
console.log(`New API URL: ${NEW_API_URL}`);

// Android project file paths
const androidProjectPath = './app/src/main/java/com/example/mad_gruop_ass';
const apiClientFile = path.join(androidProjectPath, 'ApiClient.kt');
const retrofitClientFile = path.join(androidProjectPath, 'api/RetrofitClient.kt');

// Update ApiClient.kt
function updateApiClient() {
    if (!fs.existsSync(apiClientFile)) {
        console.log(`❌ File not found: ${apiClientFile}`);
        return false;
    }
    
    try {
        let content = fs.readFileSync(apiClientFile, 'utf8');
        
        // Replace BASE_URL value
        const regex = /private const val BASE_URL = "http:\/\/[^"]+"\s*(?:\/\/.*)?/;
        const newLine = `private const val BASE_URL = "${NEW_API_URL}"  // Auto-updated`;
        
        if (regex.test(content)) {
            content = content.replace(regex, newLine);
            fs.writeFileSync(apiClientFile, content, 'utf8');
            console.log(`✅ Updated ApiClient.kt`);
            return true;
        } else {
            console.log(`⚠️  BASE_URL definition not found in ApiClient.kt`);
            return false;
        }
    } catch (error) {
        console.log(`❌ Failed to update ApiClient.kt: ${error.message}`);
        return false;
    }
}

// Update RetrofitClient.kt
function updateRetrofitClient() {
    if (!fs.existsSync(retrofitClientFile)) {
        console.log(`❌ File not found: ${retrofitClientFile}`);
        return false;
    }
    
    try {
        let content = fs.readFileSync(retrofitClientFile, 'utf8');
        
        // Replace DEFAULT_BASE_URL value
        const regex = /private const val DEFAULT_BASE_URL = "http:\/\/[^"]+"\s*(?:\/\/.*)?/;
        const newLine = `private const val DEFAULT_BASE_URL = "${NEW_API_URL}/"  // Auto-updated`;
        
        if (regex.test(content)) {
            content = content.replace(regex, newLine);
            fs.writeFileSync(retrofitClientFile, content, 'utf8');
            console.log(`✅ Updated RetrofitClient.kt`);
            return true;
        } else {
            console.log(`⚠️  DEFAULT_BASE_URL definition not found in RetrofitClient.kt`);
            return false;
        }
    } catch (error) {
        console.log(`❌ Failed to update RetrofitClient.kt: ${error.message}`);
        return false;
    }
}

// Execute updates
console.log('\n=== Starting Android config file updates ===');
const apiClientUpdated = updateApiClient();
const retrofitClientUpdated = updateRetrofitClient();

if (apiClientUpdated || retrofitClientUpdated) {
    console.log('\n✅ Configuration files updated successfully!');
    console.log('Please rebuild the Android project to apply the new IP address configuration.');
} else {
    console.log('\n❌ Failed to update any configuration files');
    console.log('Please manually update the following address in your Android project:');
    console.log(`API URL: ${NEW_API_URL}`);
}

console.log('\n=== Network Interface Information ===');
const interfaces = os.networkInterfaces();
for (const name in interfaces) {
    const iface = interfaces[name];
    console.log(`${name}:`);
    iface.forEach(alias => {
        if (alias.family === 'IPv4') {
            console.log(`  IPv4: ${alias.address} ${alias.internal ? '(internal)' : '(external)'}`);
        }
    });
} 