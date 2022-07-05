package io.mobidoo.data.mappers

import io.mobidoo.data.entities.FlashCallItem
import io.mobidoo.data.entities.FlashCallResponse
import io.mobidoo.data.entities.StartCollectionResponse
import io.mobidoo.data.entities.tmppackage.TmpStartResponse
import io.mobidoo.data.entities.wallpaper.*
import io.mobidoo.domain.entities.FlashCall
import io.mobidoo.domain.entities.StartCollection
import io.mobidoo.domain.entities.wallpaper.Category
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper

class WallpaperApiResponseMapper {

    fun toStartCollection(response: StartCollectionResponse) : StartCollection{
        return StartCollection(
            all = response.all.map { it.tuSubCategories() },
            categories = response.categories.map { it.toCategory() },
            flashCalls = response.flashCalls.map { toFlashCall(it) },
            liveWallpapers = response.liveWallpapers.map{it.toWallpaper()},
            new = response.new.map { it.tuSubCategories() }
        )
    }

    // link in type argument
    fun toStartCollection(response: TmpStartResponse) : StartCollection{
        return StartCollection(
            all = response.categories_all.map { SubCategory(0, "", emptyList(), it.link, it.nameCategory) },
            categories = emptyList(),
            flashCalls = response.wallpapers_flash_call.array.map { FlashCall(it.url, it.preview_url, "live") },
            liveWallpapers = response.category_live.array.map{ Wallpaper(it.url, it.preview_url, "live") },
            new = response.categories_new.map { SubCategory(0, it.urlPhoto, it.array.map { Wallpaper(it.url, it.preview_url, "") }, it.link, it.nameCategory) }
        )
    }

    fun toSubCategoryList(response: SubCategoryListResponse) : List<SubCategory>{
        return response.items.map{SubCategory(it.id, it.name, it.array.map{it.toWallpaper()}, it.categoryName, it.type)}
    }

    fun CategoryResponse.toCategory(): Category{
        return Category(
            this.id,
            this.name,
            this.mainArray.map {
                it.toWallpaper()
            }
        )
    }

    fun toWallpaperList(response :WallpapersResponse) : List<Wallpaper>{
        return response.items.map { Wallpaper(
            it.url,
            it.previewUrl,
            it.type
        ) }
    }

    private fun WallpaperItem.toWallpaper(): Wallpaper{
        return Wallpaper(
            this.url,
            this.previewUrl,
            this.type
        )
    }
    private fun toFlashCall(call: FlashCallItem) : FlashCall{
        return FlashCall(
            call.url,
            call.title,
            call.type
        )
    }

    fun toFlashCallList(response: FlashCallResponse) : List<FlashCall>{
        return response.items.map{toFlashCall(it)}
    }

    fun SubCategoryResponse.tuSubCategories() : SubCategory{
        return SubCategory(
            this.id,
            this.categoryName,
            this.array.map { it -> it.toWallpaper() },
            this.name,
            this.type
        )
    }


}