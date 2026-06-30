package com.example.serviceflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceflow.R
import com.example.serviceflow.model.OrdemServico
import com.example.serviceflow.repository.ServiceFlowRepository
import com.example.serviceflow.util.Constants.FILTER_ALL_FEMININE
import com.example.serviceflow.util.Constants.STATUS_COMPLETED
import com.example.serviceflow.util.Constants.STATUS_PENDING
import com.example.serviceflow.util.UiText
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FuncUiState(
    val ordens: List<OrdemServico> = emptyList(),
    val filtroStatus: String = FILTER_ALL_FEMININE,
    val isLoading: Boolean = false
)

sealed class FuncAction {
    data object Idle : FuncAction()
    data class Error(val message: UiText) : FuncAction()
    data object OrdemConcluida : FuncAction()
}

class FuncionarioViewModel(
    private val repo: ServiceFlowRepository
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
                STATUS_PENDING -> os.status == STATUS_PENDING
                STATUS_COMPLETED -> os.status == STATUS_COMPLETED
                else -> true
            }
        }
    }

    fun concluirOrdem(ordemId: String, descricao: String) {
        if (descricao.isBlank()) {
            _action.value = FuncAction.Error(UiText.StringResource(R.string.error_missing_description))
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repo.concluirOrdem(ordemId, descricao)
            _uiState.update { it.copy(isLoading = false) }
            _action.value = result.fold(
                onSuccess = { FuncAction.OrdemConcluida },
                onFailure = { FuncAction.Error(it.message?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.error_concluding_os)) }
            )
        }
    }

    fun resetAction() { _action.value = FuncAction.Idle }
}
