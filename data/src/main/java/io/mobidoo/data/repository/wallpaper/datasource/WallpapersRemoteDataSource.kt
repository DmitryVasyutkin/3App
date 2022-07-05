package io.mobidoo.data.repository.wallpaper.datasource

import io.mobidoo.data.entities.FlashCallResponse
import io.mobidoo.data.entities.StartCollectionResponse
import io.mobidoo.data.entities.tmppackage.TmpStartResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryListResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryResponse
import io.mobidoo.data.entities.wallpaper.WallpapersResponse
import retrofit2.Response

interface WallpapersRemoteDataSource {
    suspend fun getStartCollection() : Response<StartCollectionResponse>
    suspend fun getLiveCategories() : Response<SubCategoryListResponse>
    suspend fun getFlashCalls() : Response<FlashCallResponse>
    suspend fun getSubcategories(id: Long) : Response<SubCategoryListResponse>
    suspend fun getWallpapers(id: Long) : Response<WallpapersResponse>

    suspend fun getTmpStart() : Response<TmpStartResponse>
}