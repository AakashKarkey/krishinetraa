package com.ace.krishinetra_mobile.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ace.krishinetra_mobile.data.model.PredictionResponse
import com.ace.krishinetra_mobile.data.repository.AnalysisRepository
import kotlinx.coroutines.launch

data class AnalyzeUiState(
    val selectedUri: Uri? = null,
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val result: PredictionResponse? = null,
    val error: String? = null
)

class AnalyzeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AnalysisRepository(application)

    private val _uiState = MutableLiveData<AnalyzeUiState>(AnalyzeUiState())
    val uiState: LiveData<AnalyzeUiState> = _uiState

    fun setImage(uri: Uri?) {
        _uiState.value = _uiState.value?.copy(selectedUri = uri, result = null, error = null)
    }

    fun analyze() {
        val uri = _uiState.value?.selectedUri ?: return

        _uiState.value = _uiState.value?.copy(
            isUploading = true,
            uploadProgress = 0,
            error = null,
            result = null
        )

        simulateProgress()

        viewModelScope.launch {
            val result = repository.analyzeImage(uri)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value?.copy(
                        isUploading = false,
                        uploadProgress = 100,
                        result = response
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value?.copy(
                        isUploading = false,
                        uploadProgress = 0,
                        error = error.message ?: "Analysis failed"
                    )
                }
            )
        }
    }

    private fun simulateProgress() {
        viewModelScope.launch {
            for (i in 1..19) {
                kotlinx.coroutines.delay(150)
                _uiState.value = _uiState.value?.copy(uploadProgress = i * 5)
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value?.copy(result = null, error = null)
    }
}
