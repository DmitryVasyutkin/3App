package io.mobidoo.data.entities

import com.google.gson.annotations.SerializedName
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper

data class StartCollectionResponse(
    @SerializedName("flashCalls")
    val flashCalls: List<FlashCallItem>,
    @SerializedName("liveWallpapers")
    val liveWallpapers: List<Wallpaper>,
    @SerializedName("all")
    val all: List<SubCategory>,
    @SerializedName("new")
    val new: List<SubCategory>,
    @SerializedName("categories")
    val categories: List<SubCategory>
)
