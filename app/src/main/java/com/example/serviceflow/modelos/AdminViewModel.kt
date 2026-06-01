package com.example.serviceflow.modelos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceflow.model.OrdemServico
import com.example.serviceflow.model.User
import com.example.serviceflow.repository.ServiceFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminUiState(
    val ordens: List<OrdemServico> = emptyList(),
    val funcionarios: List<User> = emptyList(),
    val filtroStatus: String = "todas",
    val filtroDepartamento: String = "todos",
    val filtroFuncionario: String = "todos",
    val isLoading: Boolean = false,
    val erro: String? = null
)

sealed class AdminAction {
    object Idle : AdminAction()
    data class Error(val message: String) : AdminAction()
    object OrdemCriada : AdminAction()
}

class AdminViewModel(
    private val repo: ServiceFlowRepository = ServiceFlowRepository()
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
                "pendente" -> os.status == "pendente"
                "concluida" -> os.status == "concluida"
                else -> true
            }
            val matchDept = state.filtroDepartamento == "todos" || os.departamento == state.filtroDepartamento
            val matchFunc = state.filtroFuncionario == "todos" || os.funcionarioId == state.filtroFuncionario
            matchStatus && matchDept && matchFunc
        }
    }

    fun departamentosDisponiveis(): List<String> = _uiState.value.ordens.map { it.departamento }.distinct().sorted()

    fun criarOrdem(titulo: String, descricao: String, departamento: String, funcionario: User) {
        if (titulo.isBlank() || descricao.isBlank() || departamento.isBlank()) {
            _action.value = AdminAction.Error("Preencha todos os campos obrigatórios.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repo.criarOrdem(titulo, descricao, departamento, funcionario.id, funcionario.nome)
            _uiState.update { it.copy(isLoading = false) }
            _action.value = result.fold(
                onSuccess = { AdminAction.OrdemCriada },
                onFailure = { AdminAction.Error(it.message ?: "Erro ao criar OS") }
            )
        }
    }

    fun resetAction() { _action.value = AdminAction.Idle }
}
