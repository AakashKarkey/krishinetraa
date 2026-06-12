package com.ace.krishinetra_mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class ProfileUiState(
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val analysisCount: Int = 0
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableLiveData(ProfileUiState())
    val uiState: LiveData<ProfileUiState> = _uiState

    init {
        _uiState.value = ProfileUiState(
            isLoggedIn = true,
            userName = "KrishiNetra User",
            userEmail = "user@krishinetra.app",
            analysisCount = 0
        )
    }

    fun signOut() {
        _uiState.value = ProfileUiState()
    }
}
