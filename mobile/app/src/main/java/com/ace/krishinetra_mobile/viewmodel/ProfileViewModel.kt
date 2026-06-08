package com.ace.krishinetra_mobile.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ProfileUiState(
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val analysisCount: Int = 0
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableLiveData(ProfileUiState())
    val uiState: LiveData<ProfileUiState> = _uiState

    fun login(email: String, password: String) {
        _uiState.value = ProfileUiState(
            isLoggedIn = true,
            userName = email.substringBefore("@"),
            userEmail = email
        )
    }

    fun signUp(name: String, email: String, password: String) {
        _uiState.value = ProfileUiState(
            isLoggedIn = true,
            userName = name,
            userEmail = email
        )
    }

    fun signOut() {
        _uiState.value = ProfileUiState()
    }
}
