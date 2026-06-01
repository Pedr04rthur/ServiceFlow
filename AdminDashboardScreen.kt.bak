package com.example.serviceflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.serviceflow.model.OrdemServico
import com.example.serviceflow.model.User
import com.example.serviceflow.ui.components.OSCard
import com.example.serviceflow.ui.components.SectionHeader
import com.example.serviceflow.ui.components.FiltroChips
import com.example.serviceflow.ui.theme.Azul500
import com.example.serviceflow.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentUser: User,
    viewModel: AdminViewModel,
    onOSClick: (OrdemServico) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var termoBusca by remember { mutableStateOf("") }

    // Para depuração: se a lista de OS estiver carregando ou vazia
    if (uiState.ordens.isEmpty() && !uiState.isLoading) {
        // Mostrar uma mensagem centralizada
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma ordem de serviço encontrada.\nClique no + para criar uma nova.")
        }
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ordens de Serviço") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Azul500)) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(12.dp)) {
            OutlinedTextField(value = termoBusca, onValueChange = { termoBusca = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Buscar por título") })
            SectionHeader("Filtrar por status")
            FiltroChips(listOf("todas" to "Todas", "pendente" to "Pendentes", "concluida" to "Concluídas"), selecionado = uiState.filtroStatus, onSelect = { viewModel.setFiltroStatus(it) })
            val ordens = viewModel.ordensFiltradas().filter { termoBusca.isBlank() || it.titulo.contains(termoBusca, ignoreCase = true) }
            if (ordens.isEmpty()) {
                Text("Nenhuma OS com esse filtro.")
            } else {
                LazyColumn { items(ordens) { os -> OSCard(os = os, onClick = { onOSClick(os) }) } }
            }
        }
    }
}
