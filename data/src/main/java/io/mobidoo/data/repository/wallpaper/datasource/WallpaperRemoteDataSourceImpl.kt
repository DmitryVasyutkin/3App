package io.mobidoo.data.repository.wallpaper.datasource

import io.mobidoo.data.api.ApiService
import io.mobidoo.data.entities.StartCollectionResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryListResponse
import io.mobidoo.data.entities.wallpaper.WallpapersResponse
import retrofit2.Response

class WallpaperRemoteDataSourceImpl(
    private val apiService: ApiService
) : WallpapersRemoteDataSource{
    override suspend fun getStartCollection(): Response<StartCollectionResponse> {
        return apiService.getStartCollection()
    }

    override suspend fun getSubcategories(link: String): Response<SubCategoryListResponse> {
        return apiService.getSubcategories(link)
    }

    override suspend fun getWallpapers(link: String): Response<WallpapersResponse> {
        return apiService.getWallpapers(link)
    }

}