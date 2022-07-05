package io.mobidoo.data.repository.wallpaper.datasource

import io.mobidoo.data.api.ApiService
import io.mobidoo.data.entities.FlashCallResponse
import io.mobidoo.data.entities.StartCollectionResponse
import io.mobidoo.data.entities.tmppackage.TmpStartResponse
import io.mobidoo.data.entities.wallpaper.SubCategoryListResponse
import io.mobidoo.data.entities.wallpaper.WallpapersResponse
import retrofit2.Response

class WallpaperRemoteDataSourceImpl(
    private val apiService: ApiService
) : WallpapersRemoteDataSource{
    override suspend fun getStartCollection(): Response<StartCollectionResponse> {
        return apiService.getStartCollection()
    }

    override suspend fun getLiveCategories(): Response<SubCategoryListResponse> {
        return apiService.getLiveCategories()
    }

    override suspend fun getFlashCalls(): Response<FlashCallResponse> {
        return apiService.getFlashCalls()
    }

    override suspend fun getSubcategories(id: Long): Response<SubCategoryListResponse> {
        return apiService.getSubcategories(id)
    }

    override suspend fun getWallpapers(id: Long): Response<WallpapersResponse> {
        return apiService.getWallpapers(id)
    }

    override suspend fun getTmpStart(): Response<TmpStartResponse> {
        return apiService.getTmpStartCollection()
    }
}