@echo off
echo 🚀 开始测试items数据更新...
echo.

REM 设置API基础URL
set API_URL=http://192.168.0.103:5000

echo 📋 步骤1: 获取当前所有items数据
curl -X GET "%API_URL%/api/items" -H "Content-Type: application/json"
echo.
echo.

echo 📝 步骤2: 更新第一个item (假设ID为1)
curl -X PATCH "%API_URL%/api/items/1" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\": \"测试修改标题 - 更新版本\", \"description\": \"这是一个测试描述，用于验证PATCH接口\", \"price\": \"99.99\", \"status\": \"Available\"}"
echo.
echo.

echo 🔄 步骤3: 批量更新多个字段
curl -X PATCH "%API_URL%/api/items/1" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\": \"批量更新标题\", \"price\": \"888.88\", \"status\": \"Sold\", \"views\": 999, \"likes\": 88}"
echo.
echo.

echo 🔍 步骤4: 验证更新结果
curl -X GET "%API_URL%/api/items" -H "Content-Type: application/json"
echo.
echo.

echo ⚠️  步骤5: 测试错误情况 - 更新不存在的item
curl -X PATCH "%API_URL%/api/items/99999" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\": \"不存在的item\"}"
echo.
echo.

echo ⚠️  步骤6: 测试错误情况 - 空更新
curl -X PATCH "%API_URL%/api/items/1" ^
  -H "Content-Type: application/json" ^
  -d "{}"
echo.
echo.

echo 🎉 测试完成！
pause
