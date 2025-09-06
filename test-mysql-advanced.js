const mysql = require('mysql2/promise');

// 不同的连接配置选项
const connectionConfigs = [
  {
    name: '默认root用户连接',
    config: {
      host: '192.168.56.1',
      port: 3306,
      user: 'root',
      password: '',  // 请根据实际情况修改
      connectTimeout: 5000
    }
  },
  {
    name: '带密码的root用户连接',
    config: {
      host: '192.168.56.1',
      port: 3306,
      user: 'root',
      password: '1234',  // 用户提供的密码
      connectTimeout: 5000
    }
  },
  {
    name: '远程用户连接',
    config: {
      host: '192.168.56.1',
      port: 3306,
      user: 'remote_user',
      password: 'password',  // 请根据实际情况修改
      connectTimeout: 5000
    }
  }
];

async function testConnection(configObj) {
  console.log(`\n🔍 测试: ${configObj.name}`);
  console.log('=' .repeat(50));
  
  let connection;
  
  try {
    console.log(`📍 服务器: ${configObj.config.host}:${configObj.config.port}`);
    console.log(`👤 用户名: ${configObj.config.user}`);
    console.log(`🔐 密码: ${configObj.config.password ? '***' : '(空)'}`);
    console.log('⏳ 连接中...');
    
    connection = await mysql.createConnection(configObj.config);
    
    console.log('✅ 连接成功！');
    
    // 执行基本查询
    const [rows] = await connection.execute('SELECT VERSION() as version, USER() as current_user');
    console.log(`📊 MySQL版本: ${rows[0].version}`);
    console.log(`👤 当前用户: ${rows[0].current_user}`);
    
    return { success: true, config: configObj };
    
  } catch (error) {
    console.log('❌ 连接失败');
    console.log(`🔍 错误代码: ${error.code}`);
    console.log(`💬 错误信息: ${error.message}`);
    
    // 提供针对性的解决建议
    switch(error.code) {
      case 'ECONNREFUSED':
        console.log('💡 建议: MySQL服务可能未运行或端口被阻止');
        break;
      case 'ER_ACCESS_DENIED_ERROR':
        console.log('💡 建议: 用户名或密码错误');
        break;
      case 'ER_HOST_NOT_PRIVILEGED':
        console.log('💡 建议: 主机没有连接权限，需要在MySQL中授权');
        console.log('   执行: GRANT ALL PRIVILEGES ON *.* TO \'用户名\'@\'%\' IDENTIFIED BY \'密码\';');
        break;
      case 'ENOTFOUND':
        console.log('💡 建议: 无法解析主机名，检查IP地址');
        break;
      case 'ETIMEDOUT':
        console.log('💡 建议: 连接超时，检查网络和防火墙');
        break;
      default:
        console.log('💡 建议: 检查MySQL配置和网络连接');
    }
    
    return { success: false, config: configObj, error };
    
  } finally {
    if (connection) {
      await connection.end();
    }
  }
}

async function testPortConnectivity() {
  console.log('\n🌐 测试端口连通性');
  console.log('=' .repeat(30));
  
  const net = require('net');
  
  return new Promise((resolve) => {
    const socket = new net.Socket();
    const timeout = 5000;
    
    socket.setTimeout(timeout);
    
    socket.on('connect', () => {
      console.log('✅ 端口3306可以连接');
      socket.destroy();
      resolve(true);
    });
    
    socket.on('timeout', () => {
      console.log('❌ 连接超时');
      socket.destroy();
      resolve(false);
    });
    
    socket.on('error', (err) => {
      console.log(`❌ 连接错误: ${err.message}`);
      resolve(false);
    });
    
    console.log('🔗 正在测试端口连通性...');
    socket.connect(3306, '192.168.56.1');
  });
}

async function runAllTests() {
  console.log('🚀 MySQL连接测试工具 - 高级版');
  console.log('================================\n');
  
  // 首先测试端口连通性
  const portOpen = await testPortConnectivity();
  
  if (!portOpen) {
    console.log('\n🚨 端口连通性测试失败，请检查：');
    console.log('1. MySQL服务是否在192.168.56.1上运行');
    console.log('2. 防火墙是否阻止了3306端口');
    console.log('3. 网络连接是否正常');
    return;
  }
  
  // 测试不同的连接配置
  const results = [];
  
  for (const config of connectionConfigs) {
    const result = await testConnection(config);
    results.push(result);
    
    if (result.success) {
      console.log('\n🎉 找到可用的连接配置！');
      break;
    }
  }
  
  // 总结结果
  console.log('\n📋 测试总结');
  console.log('=' .repeat(20));
  
  const successfulConfigs = results.filter(r => r.success);
  
  if (successfulConfigs.length > 0) {
    console.log('✅ 成功的连接配置:');
    successfulConfigs.forEach(r => {
      console.log(`   - ${r.config.name}`);
    });
    
    console.log('\n🔧 推荐的连接配置:');
    const recommended = successfulConfigs[0];
    console.log('```javascript');
    console.log('const dbConfig = {');
    console.log(`  host: '${recommended.config.config.host}',`);
    console.log(`  port: ${recommended.config.config.port},`);
    console.log(`  user: '${recommended.config.config.user}',`);
    console.log(`  password: '${recommended.config.config.password}',`);
    console.log(`  database: 'your_database_name'`);
    console.log('};');
    console.log('```');
    
  } else {
    console.log('❌ 所有连接配置都失败了');
    console.log('\n🛠️ 请按照以下步骤解决问题:');
    console.log('1. 查看 mysql-setup-guide.md 文件获取详细配置指南');
    console.log('2. 在MySQL服务器上创建远程连接用户');
    console.log('3. 检查MySQL的bind-address配置');
    console.log('4. 确保防火墙允许3306端口');
  }
}

// 运行所有测试
runAllTests().catch(console.error);
