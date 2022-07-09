package io.mobidoo.a3app.entity.startcollectionitem

import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class WallpaperRecyclerItem(
    val list: List<Wallpaper> = emptyList(),
    var isAdvertising: Boolean = false
)
