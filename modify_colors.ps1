# 修改colors.xml
$colors = Get-Content "app/src/main/res/values/colors.xml" -Raw
$newColors = $colors -replace "</resources>", @"
    
    <!-- 详情页面颜色（合并自详情项目） -->
    <color name="text_primary">#FF212121</color>
    <color name="text_secondary">#FF757575</color>
    <color name="image_background">#FFE0E0E0</color>
    <color name="liked_color">#FF4CAF50</color>
    <color name="default_button_color">#FFE0E0E0</color>
    <color name="primary_color">#FF2196F3</color>
    <color name="button_background">#FFF0F0F0</color>
    <color name="button_disabled">#FFBDBDBD</color>
    <color name="text_disabled">#FF9E9E9E</color>
</resources>
"@
$newColors | Set-Content "app/src/main/res/values/colors.xml" -NoNewline
