package com.example.serviceflow.modelos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceflow.model.User
import com.example.serviceflow.repository.ServiceFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

class AuthViewModel(
    private val repo: ServiceFlowRepository = ServiceFlowRepository()
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getCurrentUser().collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun login(email: String, senha: String) {
        if (email.isBlank() || senha.isBlank()) {
            _loginState.value = LoginState.Error("Preencha e-mail e senha.")
            return
        }
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = repo.login(email.trim(), senha)
            _loginState.value = result.fold(
                onSuccess = { user ->
                    LoginState.Success(user)
                },
                onFailure = {
                    LoginState.Error("E-mail ou senha inválidos.")
                }
            )
        }
    }

    fun logout() {
        repo.logout()
        _loginState.value = LoginState.Idle
        _currentUser.value = null
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}