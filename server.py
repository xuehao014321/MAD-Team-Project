#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
MAD Team Project - Python服务器
替代Node.js服务器，提供用户注册和登录API
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import json
import os
import time
import random
import string
from datetime import datetime
import re

app = Flask(__name__)
CORS(app)  # 允许跨域请求

# 配置
HOST = '192.168.0.104'  # 你的IP地址
PORT = 5000
USERS_FILE = 'users.json'

def log_request(req):
    """记录请求日志"""
    timestamp = datetime.now().isoformat()
    print(f"{timestamp} - {req.method} {req.path}")
    if req.is_json:
        print(f"Request body: {req.get_json()}")

def init_users_file():
    """初始化用户数据文件"""
    if not os.path.exists(USERS_FILE):
        with open(USERS_FILE, 'w', encoding='utf-8') as f:
            json.dump([], f, ensure_ascii=False, indent=2)
        print('创建用户数据文件')
    else:
        print('用户数据文件已存在')

def read_users():
    """读取用户数据"""
    try:
        with open(USERS_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:
        print(f'读取用户数据失败: {e}')
        return []

def write_users(users):
    """写入用户数据"""
    try:
        with open(USERS_FILE, 'w', encoding='utf-8') as f:
            json.dump(users, f, ensure_ascii=False, indent=2)
        return True
    except Exception as e:
        print(f'写入用户数据失败: {e}')
        return False

def generate_user_id():
    """生成用户ID"""
    timestamp = str(int(time.time() * 1000))
    random_str = ''.join(random.choices(string.ascii_lowercase + string.digits, k=9))
    return timestamp + random_str

def is_valid_email(email):
    """验证邮箱格式"""
    pattern = r'^[^\s@]+@[^\s@]+\.[^\s@]+$'
    return re.match(pattern, email) is not None

# 请求日志中间件
@app.before_request
def before_request():
    log_request(request)

# API路由

@app.route('/api/test', methods=['GET'])
def test_connection():
    """测试连接"""
    print('收到测试连接请求')
    return jsonify({
        'message': '服务器连接成功！',
        'server': f'{HOST}:{PORT}',
        'timestamp': datetime.now().isoformat(),
        'status': 'online'
    })

@app.route('/api/users', methods=['POST'])
def register_user():
    """用户注册"""
    print('收到注册请求:', request.get_json())
    
    try:
        data = request.get_json()
        
        # 获取请求数据
        username = data.get('username')
        email = data.get('email')
        password = data.get('password')
        phone = data.get('phone', '')
        gender = data.get('gender', '')
        
        # 验证必填字段
        if not username or not email or not password:
            return jsonify({
                'success': False,
                'message': '用户名、邮箱和密码为必填项'
            }), 400
        
        # 验证邮箱格式
        if not is_valid_email(email):
            return jsonify({
                'success': False,
                'message': '邮箱格式不正确'
            }), 400
        
        # 读取现有用户
        users = read_users()
        
        # 检查用户名是否已存在
        if any(user['username'] == username for user in users):
            return jsonify({
                'success': False,
                'message': '用户名已存在'
            }), 400
        
        # 检查邮箱是否已存在
        if any(user['email'] == email for user in users):
            return jsonify({
                'success': False,
                'message': '邮箱已被注册'
            }), 400
        
        # 创建新用户
        new_user = {
            'user_id': generate_user_id(),
            'username': username,
            'email': email,
            'password': password,  # 注意：实际项目中应该加密密码
            'phone': phone,
            'gender': gender,
            'distance': 0,
            'created_at': datetime.now().isoformat(),
            'avatar_url': f'https://api.dicebear.com/7.x/avataaars/svg?seed={username}'
        }
        
        # 添加到用户列表
        users.append(new_user)
        
        # 保存到文件
        if not write_users(users):
            return jsonify({
                'success': False,
                'message': '保存用户数据失败'
            }), 500
        
        # 返回成功响应（不包含密码）
        user_without_password = {k: v for k, v in new_user.items() if k != 'password'}
        
        print('用户注册成功:', user_without_password)
        return jsonify({
            'success': True,
            'message': '注册成功',
            'user': user_without_password
        }), 201
        
    except Exception as e:
        print(f'注册过程中发生错误: {e}')
        return jsonify({
            'success': False,
            'message': '服务器内部错误'
        }), 500

@app.route('/api/login', methods=['POST'])
def login_user():
    """用户登录"""
    print('收到登录请求:', request.get_json())
    
    try:
        data = request.get_json()
        
        identifier = data.get('identifier')  # 用户名或邮箱
        password = data.get('password')
        
        if not identifier or not password:
            return jsonify({
                'success': False,
                'message': '用户名/邮箱和密码为必填项'
            }), 400
        
        # 读取用户数据
        users = read_users()
        
        # 查找用户（通过用户名或邮箱）
        user = None
        for u in users:
            if u['username'] == identifier or u['email'] == identifier:
                user = u
                break
        
        if not user:
            return jsonify({
                'success': False,
                'message': '用户不存在'
            }), 401
        
        # 验证密码
        if user['password'] != password:
            return jsonify({
                'success': False,
                'message': '密码错误'
            }), 401
        
        # 登录成功，返回用户信息（不包含密码）
        user_without_password = {k: v for k, v in user.items() if k != 'password'}
        
        print('用户登录成功:', user_without_password)
        return jsonify({
            'success': True,
            'message': '登录成功',
            'user': user_without_password
        })
        
    except Exception as e:
        print(f'登录过程中发生错误: {e}')
        return jsonify({
            'success': False,
            'message': '服务器内部错误'
        }), 500

@app.route('/api/users', methods=['GET'])
def get_all_users():
    """获取所有用户"""
    print('收到获取用户列表请求')
    
    try:
        users = read_users()
        
        # 移除密码字段
        users_without_passwords = []
        for user in users:
            user_without_password = {k: v for k, v in user.items() if k != 'password'}
            users_without_passwords.append(user_without_password)
        
        print(f'返回 {len(users_without_passwords)} 个用户')
        return jsonify(users_without_passwords)
        
    except Exception as e:
        print(f'获取用户列表失败: {e}')
        return jsonify({
            'success': False,
            'message': '获取用户列表失败'
        }), 500

@app.route('/api/users/<user_id>', methods=['GET'])
def get_user_by_id(user_id):
    """根据ID获取用户"""
    print(f'收到获取用户请求, ID: {user_id}')
    
    try:
        users = read_users()
        user = None
        
        for u in users:
            if u['user_id'] == user_id:
                user = u
                break
        
        if not user:
            return jsonify({
                'success': False,
                'message': '用户不存在'
            }), 404
        
        # 移除密码字段
        user_without_password = {k: v for k, v in user.items() if k != 'password'}
        
        print('返回用户信息:', user_without_password)
        return jsonify(user_without_password)
        
    except Exception as e:
        print(f'获取用户信息失败: {e}')
        return jsonify({
            'success': False,
            'message': '获取用户信息失败'
        }), 500

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({
        'status': 'healthy',
        'timestamp': datetime.now().isoformat(),
        'server': f'{HOST}:{PORT}'
    })

@app.errorhandler(404)
def not_found(error):
    """404处理"""
    return jsonify({
        'success': False,
        'message': f'API路径不存在: {request.method} {request.path}'
    }), 404

@app.errorhandler(500)
def internal_error(error):
    """服务器错误处理"""
    print(f'服务器错误: {error}')
    return jsonify({
        'success': False,
        'message': '服务器内部错误'
    }), 500

if __name__ == '__main__':
    print('🚀 启动Python服务器...')
    
    # 初始化用户数据文件
    init_users_file()
    
    # 启动服务器
    print(f'📍 服务器地址: http://localhost:{PORT}')
    print(f'📍 网络地址: http://{HOST}:{PORT}')
    print(f'📝 可用的API端点:')
    print(f'   POST /api/users         - 用户注册')
    print(f'   POST /api/login         - 用户登录')
    print(f'   GET  /api/users         - 获取所有用户')
    print(f'   GET  /api/test          - 测试连接')
    print('=' * 50)
    
    try:
        app.run(host='0.0.0.0', port=PORT, debug=True)
    except KeyboardInterrupt:
        print('\n收到关闭信号，正在关闭服务器...')
    except Exception as e:
        print(f'启动服务器失败: {e}') 