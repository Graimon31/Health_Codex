package com.example.healthcodex.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcodex.HealthCodexApp
import com.example.healthcodex.data.network.AuthRepository
import com.example.healthcodex.data.prefs.PrefsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HealthCodexApp
    private val authRepository = app.authRepository
    private val prefs: PrefsRepository = app.prefsRepository

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val url = prefs.baseUrl.first().orEmpty()
            _state.value = _state.value.copy(serverUrl = url)
        }
    }

    fun updateServerUrl(value: String) {
        _state.value = _state.value.copy(serverUrl = value, error = null)
    }

    fun updateEmail(value: String) {
        _state.value = _state.value.copy(email = value, error = null)
    }

    fun updatePassword(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    fun login() {
        val s = _state.value
        if (s.serverUrl.isBlank()) {
            _state.value = s.copy(error = "Укажите адрес сервера")
            return
        }
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Заполните все поля")
            return
        }
        _state.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            prefs.setBaseUrl(s.serverUrl.trim())
            when (val result = authRepository.login(s.email.trim(), s.password)) {
                is AuthRepository.AuthResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, isLoggedIn = true)
                }
                is AuthRepository.AuthResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.value = LoginUiState()
        }
    }

    companion object {
        fun factory(application: Application): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return LoginViewModel(application) as T
                    }
                    return super.create(modelClass)
                }
            }
    }
}

data class LoginUiState(
    val serverUrl: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)
