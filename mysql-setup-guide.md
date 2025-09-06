# MySQL远程连接配置指南

## 🎯 当前状态
✅ Node.js和mysql2驱动已正确安装  
✅ 可以连接到MySQL服务器（192.168.56.1:3306）  
❌ MySQL服务器不允许远程主机连接  

## 🔧 需要解决的问题

**错误信息**: `Host 'WIN-RO6GV46HOF0' is not allowed to connect to this MySQL server`

这个错误表示MySQL服务器拒绝来自你当前主机的连接请求。

## 🛠️ 解决方案

### 1. 登录到MySQL服务器 (192.168.56.1)

首先，你需要在MySQL服务器上执行以下操作：

```bash
# 登录到MySQL (在MySQL服务器上执行)
mysql -u root -p
```

### 2. 创建允许远程连接的用户

```sql
-- 创建一个允许从任何主机连接的用户
CREATE USER 'remote_user'@'%' IDENTIFIED BY 'your_password';

-- 或者，只允许从特定IP连接 (更安全)
CREATE USER 'remote_user'@'192.168.56.%' IDENTIFIED BY 'your_password';

-- 授予所有权限 (根据需要调整权限)
GRANT ALL PRIVILEGES ON *.* TO 'remote_user'@'%';

-- 或者只授予特定数据库的权限
GRANT ALL PRIVILEGES ON test.* TO 'remote_user'@'%';

-- 刷新权限
FLUSH PRIVILEGES;
```

### 3. 修改MySQL配置文件

确保MySQL配置允许远程连接：

#### 在Linux/Unix系统上：
编辑 `/etc/mysql/mysql.conf.d/mysqld.cnf` 或 `/etc/mysql/my.cnf`

```ini
[mysqld]
bind-address = 0.0.0.0  # 允许所有IP连接
# 或者
bind-address = 192.168.56.1  # 只绑定到特定IP
```

#### 在Windows系统上：
编辑 `my.ini` 文件 (通常在MySQL安装目录下)

```ini
[mysqld]
bind-address = 0.0.0.0
```

### 4. 重启MySQL服务

```bash
# Linux/Unix
sudo systemctl restart mysql

# Windows (以管理员身份运行命令提示符)
net stop mysql
net start mysql
```

### 5. 检查防火墙设置

确保MySQL端口3306在防火墙中是开放的：

#### Linux (Ubuntu/Debian):
```bash
sudo ufw allow 3306
```

#### Windows:
在Windows防火墙中添加入站规则，允许端口3306

## 🧪 测试连接

完成上述配置后，更新测试脚本中的用户名和密码：

```javascript
const dbConfig = {
  host: '192.168.56.1',
  port: 3306,
  user: 'remote_user',      // 使用新创建的用户
  password: 'your_password', // 使用设置的密码
  database: 'test'
};
```

然后运行测试：
```bash
node test-mysql.js
```

## 🔒 安全建议

1. **不要使用root用户进行远程连接**
2. **使用强密码**
3. **只授予必要的权限**
4. **考虑使用SSL连接**
5. **限制允许连接的IP范围**

## 📝 快速修复脚本

如果你想快速允许当前主机连接，可以在MySQL服务器上执行：

```sql
-- 允许root用户从当前主机连接 (不推荐用于生产环境)
GRANT ALL PRIVILEGES ON *.* TO 'root'@'192.168.56.%' IDENTIFIED BY 'your_root_password';
FLUSH PRIVILEGES;
```

## 🆘 如果仍然有问题

1. 检查MySQL错误日志
2. 确认MySQL服务正在运行
3. 使用 `telnet 192.168.56.1 3306` 测试端口连通性
4. 检查网络连接和路由设置

