package io.mobidoo.a3app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.mobidoo.a3app.entity.uistate.allcollectionstate.WallCategoriesUIState
import io.mobidoo.a3app.entity.uistate.ringtonestate.RingtoneCategoriesUIState
import io.mobidoo.a3app.entity.uistate.ringtonestate.RingtonesUIState
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.usecases.GetRingtoneCategoriesUseCase
import io.mobidoo.domain.usecases.GetRingtonesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RingtonesViewModel(
    private val app: Application,
    private val getRingtoneCategoriesUseCase: GetRingtoneCategoriesUseCase,
    private val getRingtonesUseCase: GetRingtonesUseCase
) : AndroidViewModel(app) {
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    private val _categoriesUiState = MutableStateFlow(RingtoneCategoriesUIState())
    val categoriesState get() = _categoriesUiState

    private val _ringtonesUiState = MutableStateFlow(RingtonesUIState())
    val ringtonesState get() = _ringtonesUiState


    fun getCategories() = viewModelScope.launch {
        if (AppUtils.isNetworkAvailable(app)){
            when(val result = getRingtoneCategoriesUseCase.invoke()){
                is ResultData.Loading -> {
                    _categoriesUiState.value = RingtoneCategoriesUIState(
                        isLoading = true
                    )
                }
                is ResultData.Success -> {
                    _categoriesUiState.value = RingtoneCategoriesUIState(false, result.value, false )
                }
                is ResultData.Failure -> {
                    _errorMessage.value = result.message
                    delay(3000)
                    _errorMessage.value = null
                }
            }
        }else{

        }
    }

    fun getRingtones(link: String) = viewModelScope.launch {
        if (AppUtils.isNetworkAvailable(app)){
            when(val result = getRingtonesUseCase.invoke(link)){
                is ResultData.Loading -> {
                    _ringtonesUiState.value = RingtonesUIState(isLoading = true)
                }
                is ResultData.Success -> {
                    _ringtonesUiState.value = RingtonesUIState(false, result.value, false )
                }
                is ResultData.Failure -> {
                    _errorMessage.value = result.message
                    delay(3000)
                    _errorMessage.value = null
                }
            }
        }else{

        }
    }
}