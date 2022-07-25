package io.mobidoo.a3app.entity.startcollectionitem

import com.google.android.gms.ads.nativead.NativeAd
import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class WallpaperRecyclerItem(
    val list: List<Wallpaper> = emptyList(),
    var isAdvertising: Boolean = false,
    var nativeAd: NativeAd? = null
)
