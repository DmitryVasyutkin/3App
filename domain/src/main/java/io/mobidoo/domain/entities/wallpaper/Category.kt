package io.mobidoo.domain.entities.wallpaper

data class Category(
    val id: Long,
    val name: String,
    val mainArray: List<Wallpaper>
)
