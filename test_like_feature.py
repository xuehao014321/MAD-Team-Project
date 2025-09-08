#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试点赞功能
"""

import requests
import json
import time

# API基础URL
BASE_URL = "http://192.168.0.103:5000"

def test_like_feature():
    print("🚀 开始测试点赞功能...")
    
    try:
        # 步骤1: 获取当前items数据
        print("\n📋 步骤1: 获取当前items数据")
        response = requests.get(f"{BASE_URL}/api/items")
        if response.status_code == 200:
            items = response.json()
            print(f"✅ 成功获取 {len(items)} 个items")
            
            # 找到第一个item进行测试
            if items:
                test_item = items[0]
                item_id = test_item['item_id']
                original_likes = test_item['likes']
                print(f"📦 测试item: ID={item_id}, 标题='{test_item['title']}', 当前likes={original_likes}")
                
                # 步骤2: 测试点赞功能
                print(f"\n👍 步骤2: 测试点赞功能 - 将likes从{original_likes}增加到{original_likes + 1}")
                
                # 模拟点赞 - 增加likes数量
                like_data = {
                    "likes": original_likes + 1,
                    "is_liked": True
                }
                
                print(f"   发送数据: {json.dumps(like_data, ensure_ascii=False, indent=2)}")
                
                patch_response = requests.patch(
                    f"{BASE_URL}/api/items/{item_id}",
                    json=like_data,
                    headers={'Content-Type': 'application/json'}
                )
                
                if patch_response.status_code == 200:
                    result = patch_response.json()
                    print(f"✅ 点赞成功: {result}")
                    
                    # 步骤3: 验证更新结果
                    print(f"\n🔍 步骤3: 验证更新结果")
                    verify_response = requests.get(f"{BASE_URL}/api/items")
                    if verify_response.status_code == 200:
                        updated_items = verify_response.json()
                        updated_item = next((item for item in updated_items if item['item_id'] == item_id), None)
                        
                        if updated_item:
                            print(f"📦 更新后的item:")
                            print(f"   ID: {updated_item['item_id']}")
                            print(f"   标题: {updated_item['title']}")
                            print(f"   likes: {updated_item['likes']} (原始: {original_likes})")
                            print(f"   is_liked: {updated_item.get('is_liked', 'N/A')}")
                            
                            if updated_item['likes'] == original_likes + 1:
                                print("✅ likes数量更新成功！")
                            else:
                                print(f"❌ likes数量更新失败，期望: {original_likes + 1}, 实际: {updated_item['likes']}")
                        else:
                            print("❌ 找不到更新后的item")
                    else:
                        print(f"❌ 验证失败: {verify_response.status_code}")
                        
                else:
                    print(f"❌ 点赞失败: {patch_response.status_code} - {patch_response.text}")
                    
                # 步骤4: 测试取消点赞
                print(f"\n👎 步骤4: 测试取消点赞 - 将likes从{original_likes + 1}减少到{original_likes}")
                
                unlike_data = {
                    "likes": original_likes,
                    "is_liked": False
                }
                
                print(f"   发送数据: {json.dumps(unlike_data, ensure_ascii=False, indent=2)}")
                
                patch_response2 = requests.patch(
                    f"{BASE_URL}/api/items/{item_id}",
                    json=unlike_data,
                    headers={'Content-Type': 'application/json'}
                )
                
                if patch_response2.status_code == 200:
                    result2 = patch_response2.json()
                    print(f"✅ 取消点赞成功: {result2}")
                    
                    # 验证取消点赞结果
                    verify_response2 = requests.get(f"{BASE_URL}/api/items")
                    if verify_response2.status_code == 200:
                        final_items = verify_response2.json()
                        final_item = next((item for item in final_items if item['item_id'] == item_id), None)
                        
                        if final_item:
                            print(f"📦 最终item状态:")
                            print(f"   likes: {final_item['likes']} (期望: {original_likes})")
                            print(f"   is_liked: {final_item.get('is_liked', 'N/A')}")
                            
                            if final_item['likes'] == original_likes:
                                print("✅ 取消点赞成功！")
                            else:
                                print(f"❌ 取消点赞失败，期望: {original_likes}, 实际: {final_item['likes']}")
                        else:
                            print("❌ 找不到最终item")
                    else:
                        print(f"❌ 最终验证失败: {verify_response2.status_code}")
                else:
                    print(f"❌ 取消点赞失败: {patch_response2.status_code} - {patch_response2.text}")
                    
            else:
                print("❌ 没有找到items进行测试")
        else:
            print(f"❌ 获取items失败: {response.status_code}")
            
    except Exception as e:
        print(f"❌ 测试过程中出现错误: {e}")
    
    print("\n🎉 点赞功能测试完成！")

if __name__ == "__main__":
    test_like_feature()
