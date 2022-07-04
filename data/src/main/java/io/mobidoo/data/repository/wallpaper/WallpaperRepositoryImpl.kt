package io.mobidoo.data.repository.wallpaper

import io.mobidoo.data.repository.wallpaper.datasource.WallpapersRemoteDataSource
import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.entities.FlashCall
import io.mobidoo.domain.entities.StartCollection
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import io.mobidoo.domain.repositories.WallpaperRepository

class WallpaperRepositoryImpl(
    private val wallpapersRemoteDataSource: WallpapersRemoteDataSource
) : WallpaperRepository{
    override suspend fun getStartCollection(): ResultData<StartCollection> {
        TODO("Not yet implemented")
    }

    override suspend fun getLiveCategories(): ResultData<List<SubCategory>> {
        TODO("Not yet implemented")
    }

    override suspend fun getFlashCalls(): ResultData<List<FlashCall>> {
        TODO("Not yet implemented")
    }

    override suspend fun getSubcategories(id: Long): ResultData<List<SubCategory>> {
        TODO("Not yet implemented")
    }

    override suspend fun getWallpapers(id: Long): ResultData<List<Wallpaper>> {
        TODO("Not yet implemented")
    }
}