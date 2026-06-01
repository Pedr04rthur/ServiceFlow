package com.example.serviceflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.serviceflow.model.OrdemServico
import com.example.serviceflow.model.User
import com.example.serviceflow.ui.components.*
import com.example.serviceflow.ui.theme.Azul500
import com.example.serviceflow.viewmodel.FuncionarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuncionarioDashboardScreen(
    currentUser: User,
    viewModel: FuncionarioViewModel,
    onOSClick: (OrdemServico) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(currentUser.id) { viewModel.carregarOrdens(currentUser.id) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Minhas Ordens", fontWeight = FontWeight.Medium, fontSize = 16.sp); Text("Olá, ${currentUser.nome}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)) } },
                actions = {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) { Text(currentUser.nome.take(2).uppercase(), modifier = Modifier.padding(10.dp), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onLogout) { Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Azul500, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader("Filtrar por status")
            FiltroChips(opcoes = listOf("todas" to "Todas", "pendente" to "Pendentes", "concluida" to "Concluídas"), selecionado = uiState.filtroStatus, onSelect = { viewModel.setFiltroStatus(it) })
            val ordens = viewModel.ordensFiltradas()
            if (ordens.isEmpty()) Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("Nenhuma ordem encontrada.") }
            else LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(ordens, key = { it.id }) { OSCard(os = it, onClick = { onOSClick(it) }, mostrarFuncionario = false) } }
        }
    }
}
