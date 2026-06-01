package com.example.serviceflow.modelos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceflow.model.OrdemServico
import com.example.serviceflow.repository.ServiceFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FuncUiState(
    val ordens: List<OrdemServico> = emptyList(),
    val filtroStatus: String = "todas",
    val isLoading: Boolean = false
)

sealed class FuncAction {
    object Idle : FuncAction()
    data class Error(val message: String) : FuncAction()
    object OrdemConcluida : FuncAction()
}

class FuncionarioViewModel(
    private val repo: ServiceFlowRepository = ServiceFlowRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FuncUiState())
    val uiState: StateFlow<FuncUiState> = _uiState
    private val _action = MutableStateFlow<FuncAction>(FuncAction.Idle)
    val action: StateFlow<FuncAction> = _action

    fun carregarOrdens(funcionarioId: String) {
        viewModelScope.launch {
            repo.getOrdensDoFuncionario(funcionarioId).collect { lista ->
                _uiState.update { it.copy(ordens = lista) }
            }
        }
    }

    fun setFiltroStatus(status: String) = _uiState.update { it.copy(filtroStatus = status) }

    fun ordensFiltradas(): List<OrdemServico> {
        val state = _uiState.value
        return state.ordens.filter { os ->
            when (state.filtroStatus) {
                "pendente" -> os.status == "pendente"
                "concluida" -> os.status == "concluida"
                else -> true
            }
        }
    }

    fun concluirOrdem(ordemId: String, descricao: String) {
        if (descricao.isBlank()) {
            _action.value = FuncAction.Error("Descreva o que foi realizado antes de concluir.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repo.concluirOrdem(ordemId, descricao)
            _uiState.update { it.copy(isLoading = false) }
            _action.value = result.fold(
                onSuccess = { FuncAction.OrdemConcluida },
                onFailure = { FuncAction.Error(it.message ?: "Erro ao concluir OS") }
            )
        }
    }

    fun resetAction() { _action.value = FuncAction.Idle }
}
