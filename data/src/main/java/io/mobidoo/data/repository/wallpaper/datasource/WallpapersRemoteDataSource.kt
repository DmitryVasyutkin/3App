package io.mobidoo.data.repository.wallpaper.datasource

import io.mobidoo.domain.entities.FlashCall
import io.mobidoo.domain.entities.StartCollection
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import retrofit2.Response

interface WallpapersRemoteDataSource {
    suspend fun getStartCollection() : Response<StartCollection>
    suspend fun getLiveCategories() : Response<List<SubCategory>>
    suspend fun getFlashCalls() : Response<List<FlashCall>>
    suspend fun getSubcategories(id: Long) : Response<List<SubCategory>>
    suspend fun getWallpapers(id: Long) : Response<List<Wallpaper>>
}