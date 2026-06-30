package com.example.serviceflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceflow.R
import com.example.serviceflow.model.OrdemServico
import com.example.serviceflow.model.User
import com.example.serviceflow.repository.ServiceFlowRepository
import com.example.serviceflow.util.Constants.FILTER_ALL_FEMININE
import com.example.serviceflow.util.Constants.FILTER_ALL_MASCULINE
import com.example.serviceflow.util.Constants.STATUS_COMPLETED
import com.example.serviceflow.util.Constants.STATUS_PENDING
import com.example.serviceflow.util.UiText
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminUiState(
    val ordens: List<OrdemServico> = emptyList(),
    val funcionarios: List<User> = emptyList(),
    val filtroStatus: String = FILTER_ALL_FEMININE,
    val filtroDepartamento: String = FILTER_ALL_MASCULINE,
    val filtroFuncionario: String = FILTER_ALL_MASCULINE,
    val isLoading: Boolean = false,
    val erro: UiText? = null
)

sealed class AdminAction {
    data object Idle : AdminAction()
    data class Error(val message: UiText) : AdminAction()
    data object OrdemCriada : AdminAction()
}

class AdminViewModel(
    private val repo: ServiceFlowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState
    private val _action = MutableStateFlow<AdminAction>(AdminAction.Idle)
    val action: StateFlow<AdminAction> = _action

    init {
        viewModelScope.launch {
            repo.getTodasOrdens().collect { lista -> _uiState.update { it.copy(ordens = lista) } }
        }
        viewModelScope.launch {
            repo.getFuncionarios().collect { lista -> _uiState.update { it.copy(funcionarios = lista) } }
        }
    }

    fun setFiltroStatus(status: String) = _uiState.update { it.copy(filtroStatus = status) }
    fun setFiltroDept(dept: String) = _uiState.update { it.copy(filtroDepartamento = dept) }
    fun setFiltroFunc(id: String) = _uiState.update { it.copy(filtroFuncionario = id) }

    fun ordensFiltradas(): List<OrdemServico> {
        val state = _uiState.value
        return state.ordens.filter { os ->
            val matchStatus = when (state.filtroStatus) {
                STATUS_PENDING -> os.status == STATUS_PENDING
                STATUS_COMPLETED -> os.status == STATUS_COMPLETED
                else -> true
            }
            val matchDept = state.filtroDepartamento == FILTER_ALL_MASCULINE || os.departamento == state.filtroDepartamento
            val matchFunc = state.filtroFuncionario == FILTER_ALL_MASCULINE || os.funcionarioId == state.filtroFuncionario
            matchStatus && matchDept && matchFunc
        }
    }

    fun departamentosDisponiveis(): List<String> = _uiState.value.ordens.map { it.departamento }.distinct().sorted()

    fun criarOrdem(titulo: String, descricao: String, departamento: String, funcionario: User) {
        if (titulo.isBlank() || descricao.isBlank() || departamento.isBlank()) {
            _action.value = AdminAction.Error(UiText.StringResource(R.string.error_missing_fields))
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repo.criarOrdem(titulo, descricao, departamento, funcionario.id, funcionario.nome)
            _uiState.update { it.copy(isLoading = false) }
            _action.value = result.fold(
                onSuccess = { AdminAction.OrdemCriada },
                onFailure = { AdminAction.Error(it.message?.let { msg -> UiText.DynamicString(msg) } ?: UiText.StringResource(R.string.error_creating_os)) }
            )
        }
    }

    fun resetAction() { _action.value = AdminAction.Idle }
}
