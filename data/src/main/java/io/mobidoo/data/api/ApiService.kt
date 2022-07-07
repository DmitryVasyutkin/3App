package io.mobidoo.data.api

import io.mobidoo.data.entities.StartCollectionResponse
import io.mobidoo.data.entities.ringtone.RingtoneCategoryListResponse
import io.mobidoo.data.entities.ringtone.RingtonesResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryListResponse
import io.mobidoo.data.entities.wallpaper.WallpapersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/api/r3/wallpapers/main/")
    suspend fun getStartCollection() : Response<StartCollectionResponse>
    @GET("{link}")
    suspend fun getWallpapers(@Path("link", encoded = true) link: String) : Response<WallpapersResponse>
    @GET("{link}")
    suspend fun getSubcategories(@Path("link", encoded = true) link: String) : Response<SubCategoryListResponse>
    @GET("/api/ringtones/")
    suspend fun getRingtoneCategories() : Response<RingtoneCategoryListResponse>
    @GET("{link}")
    suspend fun getRingtones(@Path("link", encoded = true) link: String) : Response<RingtonesResponse>

}