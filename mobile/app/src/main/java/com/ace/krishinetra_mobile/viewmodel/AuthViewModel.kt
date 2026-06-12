package com.ace.krishinetra_mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableLiveData(AuthUiState())
    val uiState: LiveData<AuthUiState> = _uiState

    fun signIn(email: String, password: String) {
        _uiState.value = AuthUiState(isSuccess = true)
    }

    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
        _uiState.value = AuthUiState(isSuccess = true)
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
