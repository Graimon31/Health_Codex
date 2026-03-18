package com.example.healthcodex.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthcodex.HealthCodexApp
import com.example.healthcodex.data.network.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = (application as HealthCodexApp).authRepository

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun updateFullName(value: String) {
        _state.value = _state.value.copy(fullName = value, error = null)
    }

    fun updateEmail(value: String) {
        _state.value = _state.value.copy(email = value, error = null)
    }

    fun updatePassword(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    fun updatePasswordConfirm(value: String) {
        _state.value = _state.value.copy(passwordConfirm = value, error = null)
    }

    fun register() {
        val s = _state.value
        if (s.fullName.isBlank() || s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Заполните все поля")
            return
        }
        if (s.password != s.passwordConfirm) {
            _state.value = s.copy(error = "Пароли не совпадают")
            return
        }
        if (s.password.length < 6) {
            _state.value = s.copy(error = "Пароль должен быть не менее 6 символов")
            return
        }
        _state.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = authRepository.register(s.email.trim(), s.password, s.fullName.trim())) {
                is AuthRepository.AuthResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, isRegistered = true)
                }
                is AuthRepository.AuthResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return RegisterViewModel(application) as T
                    }
                    return super.create(modelClass)
                }
            }
    }
}

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val error: String? = null
)
