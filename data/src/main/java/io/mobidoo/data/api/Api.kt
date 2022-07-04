package io.mobidoo.data.api

import io.mobidoo.data.entities.FlashCallResponse
import io.mobidoo.data.entities.StartCollectionResponse
import io.mobidoo.data.entities.ringtone.RingtoneCategoryResponse
import io.mobidoo.data.entities.ringtone.RingtonesResponse
import io.mobidoo.data.entities.wallpaper.CategoryResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryResponse
import io.mobidoo.data.entities.wallpaper.WallpapersResponse
import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.entities.FlashCall
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import retrofit2.Response

interface Api {

    suspend fun getStartCollection() : Response<StartCollectionResponse>

    suspend fun getLiveCategories() : Response<SubCategoryResponse>

    suspend fun getFlashCalls() : Response<FlashCallResponse>

    suspend fun getWallpapers(id: Long) : Response<WallpapersResponse>

    suspend fun getSubcategories(id: Long) : Response<SubCategoryResponse>

    suspend fun getRingtoneCategories() : Response<RingtoneCategoryResponse>

    suspend fun getRingtones(id: Long) : Response<RingtonesResponse>
}