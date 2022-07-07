package io.mobidoo.data.api

import io.mobidoo.data.entities.FlashCallResponse
import io.mobidoo.data.entities.StartCollectionResponse
import io.mobidoo.data.entities.ringtone.RingtoneCategoryListResponse
import io.mobidoo.data.entities.ringtone.RingtoneCategoryResponse
import io.mobidoo.data.entities.ringtone.RingtonesResponse
import io.mobidoo.data.entities.tmppackage.TmpStartResponse
import io.mobidoo.data.entities.wallpaper.CategoryResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryListResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryResponse
import io.mobidoo.data.entities.wallpaper.WallpapersResponse
import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.entities.FlashCall
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    suspend fun getStartCollection() : Response<StartCollectionResponse>

    suspend fun getLiveCategories() : Response<SubCategoryListResponse>

    suspend fun getFlashCalls() : Response<FlashCallResponse>

    @GET("/api/wallpapers/static2/{id}")
    suspend fun getWallpapers(@Path("id") id: Long) : Response<WallpapersResponse>

    suspend fun getSubcategories(id: Long) : Response<SubCategoryListResponse>

    suspend fun getRingtoneCategories() : Response<RingtoneCategoryListResponse>

    suspend fun getRingtones(id: Long) : Response<RingtonesResponse>

    @GET("api/wallpapers/main/")
    suspend fun getTmpStartCollection() : Response<TmpStartResponse>
}