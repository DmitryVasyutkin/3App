package io.mobidoo.a3app.di.common

import dagger.Module
import dagger.Provides
import io.mobidoo.data.api.ApiService
import io.mobidoo.data.mappers.RingtoneApiResponseMapper
import io.mobidoo.data.mappers.WallpaperApiResponseMapper
import io.mobidoo.data.repository.ringtone.RingtoneRepositoryImpl
import io.mobidoo.data.repository.ringtone.datasource.RingtoneRemoteDataSource
import io.mobidoo.data.repository.ringtone.datasource.RingtoneRemoteDataSourceImpl
import io.mobidoo.data.repository.wallpaper.WallpaperRepositoryImpl
import io.mobidoo.data.repository.wallpaper.datasource.WallpaperRemoteDataSourceImpl
import io.mobidoo.data.repository.wallpaper.datasource.WallpapersRemoteDataSource
import io.mobidoo.domain.repositories.RingtoneRepository
import io.mobidoo.domain.repositories.WallpaperRepository
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Singleton
    @Provides
    fun provideWallpaperRemoteDataSource(apiService: ApiService): WallpapersRemoteDataSource{
        return WallpaperRemoteDataSourceImpl(apiService)
    }

    @Singleton
    @Provides
    fun provideRingtoneRemoteDataSource(apiService: ApiService): RingtoneRemoteDataSource{
        return RingtoneRemoteDataSourceImpl(apiService)
    }

    @Singleton
    @Provides
    fun provideWallpaperRepository(dataSource: WallpapersRemoteDataSource) : WallpaperRepository{
        return WallpaperRepositoryImpl(WallpaperApiResponseMapper(), dataSource)
    }

    @Singleton
    @Provides
    fun provideRingtoneRepository(dataSource: RingtoneRemoteDataSource) : RingtoneRepository{
        return RingtoneRepositoryImpl(RingtoneApiResponseMapper(), dataSource)
    }

}