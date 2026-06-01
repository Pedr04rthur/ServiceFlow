package com.example.serviceflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.example.serviceflow.modelos.FuncionarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuncionarioDashboardScreen(
    currentUser: User,
    viewModel: FuncionarioViewModel,
    onOSClick: (OrdemServico) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Carrega apenas as ordens atribuídas a este funcionário
    LaunchedEffect(currentUser.id) {
        viewModel.carregarOrdens(currentUser.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Minhas Ordens", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Text("Olá, ${currentUser.nome}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                actions = {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                        Text(
                            text = currentUser.nome.take(2).uppercase(),
                            modifier = Modifier.padding(10.dp),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Azul500,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader("Filtrar por status")
            FiltroChips(
                opcoes = listOf("todas" to "Todas", "pendente" to "Pendentes", "concluida" to "Concluídas"),
                selecionado = uiState.filtroStatus,
                onSelect = { viewModel.setFiltroStatus(it) }
            )

            val ordens = viewModel.ordensFiltradas()
            val pendentes = ordens.filter { it.status == "pendente" }
            val concluidas = ordens.filter { it.status == "concluida" }

            if (ordens.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma ordem de serviço atribuída a você.\nO administrador irá criar e atribuir OS para você.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pendentes.isNotEmpty() && uiState.filtroStatus != "concluida") {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                SectionHeader("Pendentes (${pendentes.size})")
                            }
                        }
                        items(pendentes, key = { it.id }) { os ->
                            OSCard(os = os, onClick = { onOSClick(os) }, mostrarFuncionario = false)
                        }
                    }

                    if (concluidas.isNotEmpty() && uiState.filtroStatus != "pendente") {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF3B6D11),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                SectionHeader("Concluídas (${concluidas.size})")
                            }
                        }
                        items(concluidas, key = { it.id }) { os ->
                            OSCard(os = os, onClick = { onOSClick(os) }, mostrarFuncionario = false)
                        }
                    }
                }
            }
        }
    }
}