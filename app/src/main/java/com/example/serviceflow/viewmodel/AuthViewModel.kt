package com.example.serviceflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceflow.R
import com.example.serviceflow.model.User
import com.example.serviceflow.repository.ServiceFlowRepository
import com.example.serviceflow.util.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: UiText) : LoginState()
}

class AuthViewModel(
    private val repo: ServiceFlowRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        viewModelScope.launch {
            repo.getCurrentUser().collect { user -> _currentUser.value = user }
        }
    }

    fun login(email: String, senha: String) {
        if (email.isBlank() || senha.isBlank()) {
            _loginState.value = LoginState.Error(UiText.StringResource(R.string.error_login_empty))
            return
        }
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = repo.login(email.trim(), senha)
            _loginState.value = result.fold(
                onSuccess = { LoginState.Success(it) },
                onFailure = { LoginState.Error(UiText.StringResource(R.string.error_login_invalid)) }
            )
        }
    }

    fun logout() {
        repo.logout()
        _loginState.value = LoginState.Idle
        _currentUser.value = null
    }

    fun resetState() { _loginState.value = LoginState.Idle }
}
