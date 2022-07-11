package io.mobidoo.a3app.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.mobidoo.domain.usecases.GetRingtoneCategoriesUseCase
import io.mobidoo.domain.usecases.GetRingtonesUseCase

class RingtonesViewModelFactory(
    private val app: Application,
    private val getRingtoneCategoriesUseCase: GetRingtoneCategoriesUseCase,
    private val getRingtonesUseCase: GetRingtonesUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RingtonesViewModel(app, getRingtoneCategoriesUseCase, getRingtonesUseCase) as T
    }
}