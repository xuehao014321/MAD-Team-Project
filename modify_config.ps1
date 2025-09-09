# 修改AndroidManifest.xml
$manifest = Get-Content "app/src/main/AndroidManifest.xml" -Raw
$newActivity = @"
        
        <!-- 新增：物品详情页面 -->
        <activity
            android:name=".ItemDetailActivity"
            android:exported="false"
            android:parentActivityName=".MainActivity" />
        
    </application>
"@
$manifest = $manifest -replace "    </application>", $newActivity
$manifest | Set-Content "app/src/main/AndroidManifest.xml" -NoNewline
