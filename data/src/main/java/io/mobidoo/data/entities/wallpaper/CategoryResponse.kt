package io.mobidoo.data.entities.wallpaper

data class CategoryResponse(
    val id: Long,
    val name: String,
    val mainArray: List<WallpaperItem>
)
