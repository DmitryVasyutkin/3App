package io.mobidoo.data.repository.wallpaper.datasource

import io.mobidoo.data.entities.StartCollectionResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryListResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryResponse
import io.mobidoo.data.entities.wallpaper.WallpaperItem
import io.mobidoo.data.entities.wallpaper.WallpapersResponse
import retrofit2.Response

interface WallpapersRemoteDataSource {
    suspend fun getStartCollection() : Response<StartCollectionResponse>
    suspend fun getSubcategories(link: String) : Response<List<SubCategoryResponse>>
    suspend fun getWallpapers(link: String) : Response<List<WallpaperItem>>

}