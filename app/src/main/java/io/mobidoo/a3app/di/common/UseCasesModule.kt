package io.mobidoo.a3app.di.common

import dagger.Module
import dagger.Provides
import io.mobidoo.domain.repositories.RingtoneRepository
import io.mobidoo.domain.repositories.WallpaperRepository
import io.mobidoo.domain.usecases.*
import javax.inject.Singleton

@Module
class UseCasesModule {

    @Provides
    fun getStartConnectionUseCase(wallpaperRepository: WallpaperRepository):GetStartCollectionUseCase = GetStartCollectionUseCase(wallpaperRepository)

    @Provides
    fun getWallCategoriesUseCase(wallpaperRepository: WallpaperRepository):GetSubCategoriesUseCase = GetSubCategoriesUseCase(wallpaperRepository)
    @Provides
    fun getWallpapersUseCase(wallpaperRepository: WallpaperRepository):GetWallpapersUseCase = GetWallpapersUseCase(wallpaperRepository)
    @Provides
    fun getRingtoneCategoriesUseCase(ringtoneRepository: RingtoneRepository):GetRingtoneCategoriesUseCase = GetRingtoneCategoriesUseCase(ringtoneRepository)
    @Provides
    fun getRingtonesUseCase(ringtoneRepository: RingtoneRepository):GetRingtonesUseCase = GetRingtonesUseCase(ringtoneRepository)
}