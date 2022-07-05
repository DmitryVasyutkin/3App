package io.mobidoo.data.entities

import com.google.gson.annotations.SerializedName
import io.mobidoo.data.entities.wallpaper.CategoryResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryResponse
import io.mobidoo.data.entities.wallpaper.WallpaperItem
import io.mobidoo.data.entities.wallpaper.WallpapersResponse
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class StartCollectionResponse(
    @SerializedName("flashCalls")
    val flashCalls: List<FlashCallItem>,
    @SerializedName("liveWallpapers")
    val liveWallpapers: List<WallpaperItem>,
    @SerializedName("all")
    val all: List<SubCategoryResponse>,
    @SerializedName("new")
    val new: List<SubCategoryResponse>,
    @SerializedName("categories")
    val categories: List<CategoryResponse>
)
