package io.mobidoo.data.api

import io.mobidoo.data.entities.StartCollectionResponse
import io.mobidoo.data.entities.ringtone.RingtoneCategoryListResponse
import io.mobidoo.data.entities.ringtone.RingtoneCategoryResponse
import io.mobidoo.data.entities.ringtone.RingtonesResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryListResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryResponse
import io.mobidoo.data.entities.wallpaper.WallpaperItem
import io.mobidoo.data.entities.wallpaper.WallpapersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/api/r3/wallpapers/main/")
    suspend fun getStartCollection() : Response<StartCollectionResponse>
    @GET("{link}")
    suspend fun getWallpapers(@Path("link", encoded = true) link: String) : Response<List<WallpaperItem>>
    @GET("{link}")
    suspend fun getSubcategories(@Path("link", encoded = true) link: String) : Response<List<SubCategoryResponse>>
    @GET("/api/ringtones/")
    suspend fun getRingtoneCategories() : Response<List<RingtoneCategoryResponse>>
    @GET("{link}")
    suspend fun getRingtones(@Path("link", encoded = true) link: String) : Response<RingtonesResponse>

}