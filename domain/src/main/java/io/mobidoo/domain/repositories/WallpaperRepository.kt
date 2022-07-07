package io.mobidoo.domain.repositories

import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.entities.StartCollection
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper


interface WallpaperRepository {
    suspend fun getStartCollection() : ResultData<StartCollection>
    suspend fun getSubcategories(link: String) : ResultData<List<SubCategory>>
    suspend fun getWallpapers(link: String) : ResultData<List<Wallpaper>>
}