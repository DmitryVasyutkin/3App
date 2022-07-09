package io.mobidoo.a3app.di.common

import dagger.Module
import dagger.Provides
import io.mobidoo.domain.repositories.WallpaperRepository
import io.mobidoo.domain.usecases.GetStartCollectionUseCase
import io.mobidoo.domain.usecases.GetSubCategoriesUseCase
import io.mobidoo.domain.usecases.GetWallpapersUseCase
import javax.inject.Singleton

@Module
class UseCasesModule {

    @Provides
    fun getStartConnectionUseCase(wallpaperRepository: WallpaperRepository) = GetStartCollectionUseCase(wallpaperRepository)

    @Provides
    fun getWallCategoriesUseCase(wallpaperRepository: WallpaperRepository) = GetSubCategoriesUseCase(wallpaperRepository)
    @Provides
    fun getWallpapersUseCase(wallpaperRepository: WallpaperRepository) = GetWallpapersUseCase(wallpaperRepository)
}