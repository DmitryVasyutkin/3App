package io.mobidoo.a3app.entity.startcollectionitem

import com.google.android.gms.ads.nativead.NativeAd
import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class SubCategoryRecyclerItem(
    val name: String,
    val array: List<Wallpaper>,
    val wallLink: String,
    val isAdvertising: Boolean,
    var nativeAd: NativeAd? = null
)
