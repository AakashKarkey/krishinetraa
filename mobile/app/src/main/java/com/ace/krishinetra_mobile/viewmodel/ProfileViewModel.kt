package com.ace.krishinetra_mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ace.krishinetra_mobile.KrishiNetraApp
import com.clerk.api.Clerk
import com.clerk.api.network.serialization.errorMessage
import com.clerk.api.network.serialization.onFailure
import com.clerk.api.network.serialization.onSuccess
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val analysisCount: Int = 0
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableLiveData(ProfileUiState())
    val uiState: LiveData<ProfileUiState> = _uiState

    private val app = application as KrishiNetraApp

    init {
        observeClerkSession()
    }

    private fun observeClerkSession() {
        if (!app.isClerkEnabled) {
            _uiState.value = ProfileUiState()
            return
        }
        viewModelScope.launch {
            Clerk.userFlow.collectLatest { user ->
                if (user != null) {
                    _uiState.value = ProfileUiState(
                        isLoggedIn = true,
                        userName = "${user.firstName} ${user.lastName ?: ""}".trim(),
                        userEmail = user.emailAddresses?.firstOrNull()?.emailAddress ?: "",
                        analysisCount = 0
                    )
                } else {
                    _uiState.value = ProfileUiState()
                }
            }
        }
    }

    fun signOut() {
        if (!app.isClerkEnabled) {
            _uiState.value = ProfileUiState()
            return
        }
        viewModelScope.launch {
            Clerk.auth.signOut().onSuccess {
                _uiState.value = ProfileUiState()
            }
        }
    }
}