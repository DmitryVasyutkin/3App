package io.mobidoo.domain.entities

import io.mobidoo.domain.entities.wallpaper.Category
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class StartCollection(
    val flashCalls: SubCategory,
    val popular: Category,
    val new: Category,
    val all: Category,
    val liveWallpapers: Category,
    val abstract: SubCategory,
    val pattern: SubCategory
)
