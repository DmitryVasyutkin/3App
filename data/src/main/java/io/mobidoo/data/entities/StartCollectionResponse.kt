package io.mobidoo.data.entities

import com.google.gson.annotations.SerializedName
import io.mobidoo.data.entities.wallpaper.CategoryResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryResponse

data class StartCollectionResponse(
    @SerializedName("flashCalls")
    val flashCalls: SubCategoryResponse,
    @SerializedName("popular")
    val popular: CategoryResponse,
    @SerializedName("new")
    val new: CategoryResponse,
    @SerializedName("live")
    val liveWallpapers: CategoryResponse,
    @SerializedName("all")
    val all: CategoryResponse,
    @SerializedName("abstract")
    val abstract: SubCategoryResponse,
    @SerializedName("pattern")
    val pattern: SubCategoryResponse,
)
