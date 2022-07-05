package io.mobidoo.a3app.di.common

import dagger.Module
import dagger.Provides
import io.mobidoo.domain.repositories.WallpaperRepository
import io.mobidoo.domain.usecases.GetStartCollectionUseCase
import javax.inject.Singleton

@Module
class UseCasesModule {

    @Singleton
    @Provides
    fun getStartConnectionUseCase(wallpaperRepository: WallpaperRepository) = GetStartCollectionUseCase(wallpaperRepository)
}