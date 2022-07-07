package io.mobidoo.data.entities.wallpaper

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("link")
    val subCategoriesLink: String,
    val array: List<WallpaperItem>
)