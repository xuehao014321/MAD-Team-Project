// 修复generateUserId函数，使其返回字符串格式的ID
const fs = require("fs");

// 读取当前server.js文件
const serverContent = fs.readFileSync("server.js", "utf8");

// 查找并替换generateUserId函数
const oldFunction = `// 生成用户ID - 修改为随机数字ID
function generateUserId() {
    // 生成1-100之间的随机数字作为user_id
    return Math.floor(Math.random() * 100) + 1;
}`;

const newFunction = `// 生成用户ID - 修改为随机数字ID（字符串格式）
function generateUserId() {
    // 生成1-100之间的随机数字作为user_id，返回字符串格式
    return (Math.floor(Math.random() * 100) + 1).toString();
}`;

// 替换内容
const newServerContent = serverContent.replace(oldFunction, newFunction);

// 写回文件
fs.writeFileSync("server.js", newServerContent, "utf8");

console.log(" generateUserId函数已修复，现在返回字符串格式的ID");
