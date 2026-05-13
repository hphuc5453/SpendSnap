package com.spendsnap.app.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsnap.app.data.remote.models.CategoryIconResponse
import com.spendsnap.app.data.remote.models.CategoryRequest
import com.spendsnap.app.data.remote.models.CategoryResponse
import com.spendsnap.app.data.remote.repositories.categories.ICategoryRepository
import com.spendsnap.app.data.remote.services.ApiResult
import com.spendsnap.app.data.remote.socket.InvalidatedResource
import com.spendsnap.app.data.remote.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: ICategoryRepository,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<ApiResult<List<CategoryResponse>>?>(null)
    val categoriesState: StateFlow<ApiResult<List<CategoryResponse>>?> = _categoriesState.asStateFlow()

    private val _createCategoryState = MutableStateFlow<ApiResult<Unit>?>(null)
    val createCategoryState: StateFlow<ApiResult<Unit>?> = _createCategoryState.asStateFlow()

    private val _categoryIconsState = MutableStateFlow<ApiResult<List<CategoryIconResponse>>?>(null)
    val categoryIconsState: StateFlow<ApiResult<List<CategoryIconResponse>>?> = _categoryIconsState.asStateFlow()

    init {
        viewModelScope.launch {
            socketManager.invalidations.collect { resources ->
                if (InvalidatedResource.CATEGORIES in resources) {
                    categoryRepository.invalidateCategories()
                    if (_categoriesState.value is ApiResult.Success) {
                        getCategories()
                    }
                }
            }
        }
    }

    fun getCategoryIcons() {
        viewModelScope.launch {
            _categoryIconsState.value = ApiResult.Loading(true)
            _categoryIconsState.value = categoryRepository.getCategoryIcons()
        }
    }

    fun getCategories() {
        viewModelScope.launch {
            _categoriesState.value = ApiResult.Loading(true)
            _categoriesState.value = categoryRepository.getCategories()
        }
    }

    fun createCategory(name: String, kind: String, icon: String, color: String? = null) {
        viewModelScope.launch {
            _createCategoryState.value = ApiResult.Loading(true)
            _createCategoryState.value = categoryRepository.createCategory(
                CategoryRequest(name = name, kind = kind.lowercase(), icon = icon, color = color)
            )
        }
    }

    fun resetCreateState() {
        _createCategoryState.value = null
    }
}
