package io.mobidoo.domain.entities

import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class StartCollection(
    val flashCalls: List<FlashCall>,
    val liveWallpapers: List<Wallpaper>,
    val all: List<SubCategory>,
    val new: List<SubCategory>,
    val categories: List<SubCategory>
)
