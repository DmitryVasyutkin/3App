package io.mobidoo.a3app.di.wallpaperscope

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import io.mobidoo.a3app.viewmodels.MainActivityViewModelFactory
import io.mobidoo.a3app.viewmodels.WallCategoriesViewModelFactory
import io.mobidoo.domain.usecases.GetStartCollectionUseCase
import io.mobidoo.domain.usecases.GetSubCategoriesUseCase
import io.mobidoo.domain.usecases.GetWallpapersUseCase
import javax.inject.Singleton


@Module
class WallpaperModule {

    @Provides
    fun provideMainActivityViewModelFactory(context: Context, getStartCollectionUseCase: GetStartCollectionUseCase) : MainActivityViewModelFactory {
        return MainActivityViewModelFactory(context.applicationContext as Application, getStartCollectionUseCase)
    }
    @Provides
    fun provideWallCategoriesViewModelFactory(context: Context, getSubCategoriesUseCase: GetSubCategoriesUseCase, getWallpapersUseCase: GetWallpapersUseCase) : WallCategoriesViewModelFactory{
        return WallCategoriesViewModelFactory(context.applicationContext as Application, getSubCategoriesUseCase,getWallpapersUseCase)
    }
}