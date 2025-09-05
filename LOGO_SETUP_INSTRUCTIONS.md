# NeighborLink Logo 设置说明

## 📋 如何添加您的 logo.jpg 到应用中

### 方法1：使用原始图片文件（推荐）

1. **重命名您的图片文件**
   - 将 `logo.jpg` 重命名为 `neighborlink_logo.jpg`

2. **复制到正确位置**
   - 复制 `neighborlink_logo.jpg` 到以下目录：
   ```
   app/src/main/res/drawable/neighborlink_logo.jpg
   ```

3. **删除占位符文件**
   - 删除 `app/src/main/res/drawable/neighborlink_logo.xml`
   - Android会自动使用 `.jpg` 文件

### 方法2：转换为不同尺寸（可选）

如果您想要更好的显示效果，可以创建不同密度的图片：

```
app/src/main/res/drawable-mdpi/neighborlink_logo.jpg     (150x33 px)
app/src/main/res/drawable-hdpi/neighborlink_logo.jpg     (200x44 px)  
app/src/main/res/drawable-xhdpi/neighborlink_logo.jpg    (300x66 px)
app/src/main/res/drawable-xxhdpi/neighborlink_logo.jpg   (400x88 px)
app/src/main/res/drawable-xxxhdpi/neighborlink_logo.jpg  (600x132 px)
```

## 🎯 当前布局设置

- **位置**: 顶部栏中央
- **尺寸**: 200dp × 44dp
- **背景**: 白色，带轻微阴影
- **右侧**: 用户头像按钮

## ⚡ 完成后

重新构建应用，您的logo就会显示在顶部栏中央位置！

## 🔧 如果需要调整尺寸

在 `app/src/main/res/layout/activity_main.xml` 中找到：
```xml
<ImageView
    android:id="@+id/appLogo"
    android:layout_width="200dp"
    android:layout_height="44dp"
    ...
```

修改 `layout_width` 和 `layout_height` 值来调整大小。 