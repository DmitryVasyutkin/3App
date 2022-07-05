package io.mobidoo.a3app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.mobidoo.a3app.ui.uistate.allcollectionstate.AllCollectionsUIState
import io.mobidoo.a3app.utils.AppUtils
import io.mobidoo.domain.common.ResultData
import io.mobidoo.domain.usecases.GetStartCollectionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel(
    private val app: Application,
    private val getStartCollectionUseCase: GetStartCollectionUseCase
) : AndroidViewModel(app){
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _uiStateFlow =  MutableStateFlow(AllCollectionsUIState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    fun getStartCollection() = viewModelScope.launch {
        if (AppUtils.isNetworkAvailable(app)){
            when(val result = getStartCollectionUseCase.invoke()){
                is ResultData.Loading -> {_uiStateFlow.value = AllCollectionsUIState(
                    isLoading = true
                )}
                is ResultData.Success -> {
                    _uiStateFlow.value = AllCollectionsUIState(
                        isLoading = false,
                        flashCallArray = result.value.flashCalls,
                        newPapersArray = result.value.new,
                        livePapersArray = result.value.liveWallpapers,
                        categories = result.value.categories,
                        all = result.value.all,
                        badConnection = false
                    )
                }
                is ResultData.Failure -> {
                    _errorMessage.value = result.message
                    delay(3000)
                    _errorMessage.value = null
                }
            }
        }else{
            _uiStateFlow.value = AllCollectionsUIState(
                badConnection = true
            )
        }
    }

}