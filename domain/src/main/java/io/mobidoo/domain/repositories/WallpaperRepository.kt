package io.mobidoo.domain.repositories

import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.entities.FlashCall
import io.mobidoo.domain.entities.StartCollection
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper


interface WallpaperRepository {
    suspend fun getStartCollection() : ResultData<StartCollection>
    suspend fun getLiveCategories() : ResultData<List<SubCategory>>
    suspend fun getFlashCalls() : ResultData<List<FlashCall>>
    suspend fun getSubcategories(id: Long) : ResultData<List<SubCategory>>
    suspend fun getWallpapers(id: Long) : ResultData<List<Wallpaper>>
}