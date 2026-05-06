package com.spendsnap.app.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsnap.app.data.remote.models.CategoryRequest
import com.spendsnap.app.data.remote.models.CategoryResponse
import com.spendsnap.app.data.remote.repositories.categories.ICategoryRepository
import com.spendsnap.app.data.remote.services.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: ICategoryRepository
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<ApiResult<List<CategoryResponse>>?>(null)
    val categoriesState: StateFlow<ApiResult<List<CategoryResponse>>?> = _categoriesState.asStateFlow()

    private val _createCategoryState = MutableStateFlow<ApiResult<Unit>?>(null)
    val createCategoryState: StateFlow<ApiResult<Unit>?> = _createCategoryState.asStateFlow()

    fun getCategories() {
        viewModelScope.launch {
            _categoriesState.value = ApiResult.Loading(true)
            _categoriesState.value = categoryRepository.getCategories()
        }
    }

    fun createCategory(name: String, type: String, limitBudget: Double) {
        viewModelScope.launch {
            _createCategoryState.value = ApiResult.Loading(true)
            _createCategoryState.value = categoryRepository.createCategory(
                CategoryRequest(name = name, type = type, limitBudget = limitBudget)
            )
        }
    }

    fun resetCreateState() {
        _createCategoryState.value = null
    }
}
