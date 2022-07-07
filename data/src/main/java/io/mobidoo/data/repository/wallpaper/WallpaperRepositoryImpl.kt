package io.mobidoo.data.repository.wallpaper

import io.mobidoo.data.mappers.WallpaperApiResponseMapper
import io.mobidoo.data.repository.wallpaper.datasource.WallpapersRemoteDataSource
import io.mobidoo.data.utils.TransformUtils.toResultData
import io.mobidoo.data.utils.TransformUtils.transformResult
import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.entities.StartCollection
import io.mobidoo.domain.entities.wallpaper.SubCategory
import io.mobidoo.domain.entities.wallpaper.Wallpaper
import io.mobidoo.domain.repositories.WallpaperRepository
import kotlinx.coroutines.delay
import retrofit2.Response

class WallpaperRepositoryImpl(
    private val wallpaperMapper: WallpaperApiResponseMapper,
    private val wallpapersRemoteDataSource: WallpapersRemoteDataSource
) : WallpaperRepository{

    override suspend fun getStartCollection(): ResultData<StartCollection> {
        return transformResult(wallpapersRemoteDataSource.getStartCollection().toResultData()){
            wallpaperMapper.transformStartCollection(it)
        }
    }

    override suspend fun getSubcategories(link: String): ResultData<List<SubCategory>> {
        return transformResult(wallpapersRemoteDataSource.getSubcategories(link).toResultData()){
            wallpaperMapper.transformSubCategoriesList(it)
        }
    }

    override suspend fun getWallpapers(link: String): ResultData<List<Wallpaper>> {
        return transformResult(wallpapersRemoteDataSource.getWallpapers(link).toResultData()){
            wallpaperMapper.transformWallpapersList(it)
        }
    }
}

