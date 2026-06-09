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
import com.clerk.api.signin.SignIn
import com.clerk.api.signup.SignUp
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableLiveData(AuthUiState())
    val uiState: LiveData<AuthUiState> = _uiState

    private val app = application as KrishiNetraApp

    fun signIn(email: String, password: String) {
        if (!validateEmail(email) || !validatePassword(password)) return

        if (!app.isClerkEnabled) {
            _uiState.value = AuthUiState(isSuccess = true)
            return
        }

        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            SignIn.create(
                SignIn.CreateParams.Strategy.Password(identifier = email, password = password)
            ).onSuccess {
                _uiState.value = AuthUiState(isSuccess = true)
            }.onFailure { result ->
                _uiState.value = AuthUiState(error = result.errorMessage)
            }
        }
    }

    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank()) {
            _uiState.value = AuthUiState(error = "Please enter your name")
            return
        }
        if (!validateEmail(email)) return
        if (!validatePassword(password)) return
        if (password != confirmPassword) {
            _uiState.value = AuthUiState(error = "Passwords do not match")
            return
        }

        if (!app.isClerkEnabled) {
            _uiState.value = AuthUiState(isSuccess = true)
            return
        }

        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            SignUp.create(
                SignUp.CreateParams.Standard(
                    emailAddress = email,
                    password = password
                )
            ).onSuccess {
                _uiState.value = AuthUiState(isSuccess = true)
            }.onFailure { result ->
                _uiState.value = AuthUiState(error = result.errorMessage)
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isBlank()) {
            _uiState.value = AuthUiState(error = "Please enter your email")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState(error = "Invalid email format")
            return false
        }
        return true
    }

    private fun validatePassword(password: String): Boolean {
        if (password.isBlank()) {
            _uiState.value = AuthUiState(error = "Please enter your password")
            return false
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState(error = "Password must be at least 6 characters")
            return false
        }
        return true
    }
}