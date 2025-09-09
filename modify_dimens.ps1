# 修改dimens.xml
$dimens = Get-Content "app/src/main/res/values/dimens.xml" -Raw
$newDimens = $dimens -replace "</resources>", @"
    
    <!-- 详情页面尺寸 -->
    <dimen name="detail_image_height">300dp</dimen>
    <dimen name="detail_button_height">48dp</dimen>
    <dimen name="detail_text_margin">16dp</dimen>
    <dimen name="detail_section_margin">24dp</dimen>
    <dimen name="detail_button_margin">8dp</dimen>
    <dimen name="detail_avatar_size">40dp</dimen>
    <dimen name="detail_status_padding">8dp</dimen>
    <dimen name="detail_navbar_height">56dp</dimen>
</resources>
"@
$newDimens | Set-Content "app/src/main/res/values/dimens.xml" -NoNewline
