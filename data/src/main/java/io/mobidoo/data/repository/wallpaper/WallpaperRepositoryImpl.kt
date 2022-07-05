package io.mobidoo.data.repository.wallpaper

import io.mobidoo.data.mappers.WallpaperApiResponseMapper
import io.mobidoo.data.repository.wallpaper.datasource.WallpapersRemoteDataSource
import io.mobidoo.data.utils.TransformUtils.toResultData
import io.mobidoo.data.utils.TransformUtils.transformResult
import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.entities.FlashCall
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
//        return transformResult(wallpapersRemoteDataSource.getStartCollection().toResultData()) {
//            wallpaperMapper.toStartCollection(it)
//        }
        delay(2000)
        return transformResult(wallpapersRemoteDataSource.getTmpStart().toResultData()) {
            wallpaperMapper.toStartCollection(it)
        }
    }

    override suspend fun getLiveCategories(): ResultData<List<SubCategory>> {
        return transformResult(wallpapersRemoteDataSource.getLiveCategories().toResultData()){
            wallpaperMapper.toSubCategoryList(it)
        }
    }

    override suspend fun getFlashCalls(): ResultData<List<FlashCall>> {
        return transformResult(wallpapersRemoteDataSource.getFlashCalls().toResultData()){
            wallpaperMapper.toFlashCallList(it)
        }
    }

    override suspend fun getSubcategories(id: Long): ResultData<List<SubCategory>> {
        return transformResult(wallpapersRemoteDataSource.getSubcategories(id).toResultData()){
            wallpaperMapper.toSubCategoryList(it)
        }
    }

    override suspend fun getWallpapers(id: Long): ResultData<List<Wallpaper>> {
        return transformResult(wallpapersRemoteDataSource.getWallpapers(id).toResultData()){
            wallpaperMapper.toWallpaperList(it)
        }
    }

}

