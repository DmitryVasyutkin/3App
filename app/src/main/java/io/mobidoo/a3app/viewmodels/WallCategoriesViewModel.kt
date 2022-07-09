package io.mobidoo.a3app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.mobidoo.a3app.entity.uistate.allcollectionstate.AllCollectionsUIState
import io.mobidoo.a3app.entity.uistate.allcollectionstate.WallCategoriesUIState
import io.mobidoo.a3app.entity.uistate.allcollectionstate.WallpapersUIState
import io.mobidoo.a3app.entity.uistate.allcollectionstate.toUIState
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.usecases.GetSubCategoriesUseCase
import io.mobidoo.domain.usecases.GetWallpapersUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WallCategoriesViewModel(
    private val app: Application,
    private val getSubCategoriesUseCase: GetSubCategoriesUseCase,
    private val getWallpapersUseCase: GetWallpapersUseCase
) : AndroidViewModel(app) {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _uiStateFlow =  MutableStateFlow(WallCategoriesUIState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    private val _wallUiStateFlow =  MutableStateFlow(WallpapersUIState())
    val wallUiStateFlow = _wallUiStateFlow.asStateFlow()

    fun getCategories(link: String) = viewModelScope.launch {
        if (AppUtils.isNetworkAvailable(app)){
            when(val result = getSubCategoriesUseCase.invoke(link)){
                is ResultData.Loading -> {
                    _uiStateFlow.value = WallCategoriesUIState(
                        isLoading = true
                    )
                }
                is ResultData.Success -> {
                    _uiStateFlow.value = WallCategoriesUIState(false, result.value, false )
                }
                is ResultData.Failure -> {
                    _errorMessage.value = result.message
                    delay(3000)
                    _errorMessage.value = null
                }
            }
        }else{
            _uiStateFlow.value = WallCategoriesUIState(badConnection = true )
        }
    }

    fun getWallpapers(link: String) = viewModelScope.launch {
        if (AppUtils.isNetworkAvailable(app)){
            when(val result = getWallpapersUseCase.invoke(link)){
                is ResultData.Loading -> {
                    _wallUiStateFlow.value = WallpapersUIState(
                        isLoading = true
                    )
                }
                is ResultData.Success -> {
                    _wallUiStateFlow.value = WallpapersUIState(false, result.value, false )
                }
                is ResultData.Failure -> {
                    _errorMessage.value = result.message
                    delay(3000)
                    _errorMessage.value = null
                }
            }
        }else{
            _wallUiStateFlow.value = WallpapersUIState(badConnection = true )
        }
    }
}