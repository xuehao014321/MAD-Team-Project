#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import json
import time
from datetime import datetime

# 配置
API_BASE_URL = 'http://192.168.0.103:5000'

def test_update_items():
    print('🚀 开始测试items数据更新...\n')
    
    try:
        # 1. 获取当前所有items
        print('📋 步骤1: 获取当前所有items数据')
        response = requests.get(f'{API_BASE_URL}/api/items')
        response.raise_for_status()
        
        items = response.json()
        print(f'✅ 成功获取 {len(items)} 个items')
        
        if items:
            print('📦 当前items列表:')
            for i, item in enumerate(items[:5]):  # 只显示前5个
                print(f'  {i+1}. ID: {item["item_id"]}, 标题: {item["title"]}, 价格: {item["price"]}, 状态: {item["status"]}')
        print()
        
        # 2. 测试更新第一个item
        if items:
            first_item = items[0]
            item_id = first_item['item_id']
            
            print(f'📝 步骤2: 更新item ID {item_id}')
            print(f'   原始数据: 标题="{first_item["title"]}", 价格="{first_item["price"]}", 状态="{first_item["status"]}"')
            
            # 准备更新数据
            update_data = {
                'title': f'更新后的标题 - {datetime.now().strftime("%H:%M:%S")}',
                'description': f'这是更新后的描述，修改时间: {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}',
                'price': f'{99.99 + len(items)}',
                'status': 'Available',
                'views': 100 + len(items),
                'likes': 50 + len(items)
            }
            
            print(f'   更新数据: {json.dumps(update_data, ensure_ascii=False, indent=2)}')
            
            # 发送PATCH请求
            update_response = requests.patch(f'{API_BASE_URL}/api/items/{item_id}', json=update_data)
            
            if update_response.status_code == 200:
                print(f'✅ 更新成功: {update_response.json()}')
            else:
                print(f'❌ 更新失败: {update_response.status_code} - {update_response.text}')
        print()
        
        # 3. 验证更新结果
        print('🔍 步骤3: 验证更新结果')
        verify_response = requests.get(f'{API_BASE_URL}/api/items')
        verify_response.raise_for_status()
        
        updated_items = verify_response.json()
        print(f'✅ 验证完成，当前有 {len(updated_items)} 个items')
        
        if updated_items:
            updated_item = updated_items[0]
            print('📦 更新后的第一个item:')
            print(f'   ID: {updated_item["item_id"]}')
            print(f'   标题: {updated_item["title"]}')
            print(f'   描述: {updated_item["description"]}')
            print(f'   价格: {updated_item["price"]}')
            print(f'   状态: {updated_item["status"]}')
            print(f'   浏览数: {updated_item["views"]}')
            print(f'   点赞数: {updated_item["likes"]}')
        print()
        
        # 4. 测试批量更新
        print('🔄 步骤4: 测试批量更新多个字段')
        if len(updated_items) > 1:
            second_item = updated_items[1]
            item_id = second_item['item_id']
            
            batch_update_data = {
                'title': f'批量更新标题 - {int(time.time())}',
                'price': '888.88',
                'status': 'Reserved',
                'views': 999,
                'likes': 88
            }
            
            print(f'   更新item ID {item_id} 的多个字段: {json.dumps(batch_update_data, ensure_ascii=False)}')
            
            batch_response = requests.patch(f'{API_BASE_URL}/api/items/{item_id}', json=batch_update_data)
            
            if batch_response.status_code == 200:
                print(f'✅ 批量更新成功: {batch_response.json()}')
            else:
                print(f'❌ 批量更新失败: {batch_response.status_code} - {batch_response.text}')
        print()
        
        # 5. 测试错误情况
        print('⚠️  步骤5: 测试错误情况')
        
        # 测试更新不存在的item
        print('   测试更新不存在的item (ID: 99999)...')
        non_existent_response = requests.patch(f'{API_BASE_URL}/api/items/99999', json={'title': '不存在的item'})
        if non_existent_response.status_code == 404:
            print('✅ 正确拒绝了不存在的item更新')
        else:
            print(f'❌ 意外成功更新了不存在的item: {non_existent_response.status_code}')
        
        # 测试空更新
        print('   测试空更新...')
        empty_update_response = requests.patch(f'{API_BASE_URL}/api/items/1', json={})
        if empty_update_response.status_code == 400:
            print('✅ 正确拒绝了空更新')
        else:
            print(f'❌ 意外接受了空更新: {empty_update_response.status_code}')
        print()
        
        print('🎉 测试完成！')
        
    except requests.exceptions.ConnectionError:
        print('❌ 连接失败: 请确保服务器正在运行 (http://192.168.0.103:5000)')
    except requests.exceptions.RequestException as e:
        print(f'❌ 请求错误: {e}')
    except Exception as e:
        print(f'❌ 测试过程中发生错误: {e}')

if __name__ == '__main__':
    test_update_items()
