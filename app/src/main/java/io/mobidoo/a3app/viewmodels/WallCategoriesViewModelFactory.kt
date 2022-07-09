package io.mobidoo.a3app.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.mobidoo.domain.usecases.GetSubCategoriesUseCase
import io.mobidoo.domain.usecases.GetWallpapersUseCase

class WallCategoriesViewModelFactory(
    private val app: Application,
    private val getSubCategoriesUseCase: GetSubCategoriesUseCase,
    private val getWallpapersUseCase: GetWallpapersUseCase
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WallCategoriesViewModel(app, getSubCategoriesUseCase,getWallpapersUseCase) as T
    }
}