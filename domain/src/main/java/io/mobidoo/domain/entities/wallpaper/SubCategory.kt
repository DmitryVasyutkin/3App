package io.mobidoo.domain.entities.wallpaper

import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class SubCategory(
    val id: Long,
    val name: String,
    val array: List<Wallpaper>,
    val type: String,
    val categoryName: String
)
