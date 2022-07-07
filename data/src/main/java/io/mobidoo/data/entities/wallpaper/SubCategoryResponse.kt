package io.mobidoo.data.entities.wallpaper

import com.google.gson.annotations.SerializedName
import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class SubCategoryResponse(
    val name: String,
    val array: List<WallpaperItem>,
    @SerializedName("link")
    val wallpapersLink: String,
    val type: String
)
