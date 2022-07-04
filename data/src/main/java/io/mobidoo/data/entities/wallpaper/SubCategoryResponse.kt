package io.mobidoo.data.entities.wallpaper

import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class SubCategoryResponse(
    val id: Long,
    val name: String,
    val array: List<Wallpaper>,
    val type: String,
    val categoryName: String
)
