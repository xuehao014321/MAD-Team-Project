# 修改MainActivity.kt添加详情页面导航
$mainActivity = Get-Content "app/src/main/java/com/example/mad_gruop_ass/MainActivity.kt" -Raw
$mainActivity = $mainActivity -replace "        itemAdapter = ItemAdapter\(itemList\) \{ item ->`r?`n            // Handle item click - navigate to detail page`r?`n            // TODO: Implement navigation to detail page`r?`n        \}", @"
        itemAdapter = ItemAdapter(itemList) { item ->
            // Handle item click - navigate to detail page
            val intent = Intent(this, ItemDetailActivity::class.java)
            intent.putExtra("itemId", item.itemId)
            intent.putExtra("itemTitle", item.title)
            intent.putExtra("itemDescription", item.description)
            intent.putExtra("itemPrice", item.price)
            intent.putExtra("itemImageUrl", item.imageUrl)
            intent.putExtra("itemStatus", item.status)
            intent.putExtra("itemLikes", item.likes)
            intent.putExtra("itemDistance", item.distance)
            intent.putExtra("itemCreatedAt", item.createdAt)
            intent.putExtra("itemUsername", item.username)
            startActivity(intent)
        }"@
$mainActivity | Set-Content "app/src/main/java/com/example/mad_gruop_ass/MainActivity.kt" -NoNewline
