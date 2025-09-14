﻿# 修改strings.xml
$strings = Get-Content "app/src/main/res/values/strings.xml" -Raw
$newStrings = @"
<resources>
    <string name="app_name">MAD_GRUOP_ASS</string>
    
    <!-- 详情页面字符串 -->
    <string name="item_detail_title">物品详情</string>
    <string name="back_button">返回</string>
    <string name="borrow_item">借用物品</string>
    <string name="like_item">点赞</string>
    <string name="favorite_item">收藏</string>
    <string name="back_to_main">返回主页</string>
    <string name="item_status">状态</string>
    <string name="item_available">可用</string>
    <string name="item_borrowed">已借用</string>
    <string name="item_likes">点赞数</string>
    <string name="item_favorites">收藏数</string>
    <string name="item_distance">距离</string>
    <string name="item_posted_date">发布时间</string>
    <string name="item_owner">发布者</string>
    <string name="borrow_success">借用成功</string>
    <string name="borrow_failed">借用失败</string>
    <string name="like_success">点赞成功</string>
    <string name="like_failed">点赞失败</string>
    <string name="loading">加载中...</string>
    <string name="network_error">网络错误</string>
    <string name="unknown_user">未知用户</string>
</resources>
"@
$newStrings | Set-Content "app/src/main/res/values/strings.xml" -NoNewline
