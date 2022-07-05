package io.mobidoo.a3app.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.mobidoo.domain.usecases.GetStartCollectionUseCase

class MainActivityViewModelFactory(
    private val app: Application,
    private val getStartCollectionUseCase: GetStartCollectionUseCase
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainActivityViewModel(app, getStartCollectionUseCase) as T
    }
}
