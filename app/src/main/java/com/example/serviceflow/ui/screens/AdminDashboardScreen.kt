package com.example.serviceflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.serviceflow.modelos.AdminAction
import com.example.serviceflow.modelos.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentUser: User,
    viewModel: AdminViewModel,
    onOSClick: (OrdemServico) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val action by viewModel.action.collectAsState()
    var showNovaOS by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var termoBusca by remember { mutableStateOf("") }

    LaunchedEffect(action) {
        when (action) {
            is AdminAction.OrdemCriada -> {
                showNovaOS = false
                snackbarMessage = "Ordem criada com sucesso!"
                viewModel.resetAction()
            }
            is AdminAction.Error -> {
                snackbarMessage = (action as AdminAction.Error).message
                viewModel.resetAction()
            }
            else -> {}
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Ordens de Serviço", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Text("Administrador", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
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
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Azul500,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNovaOS = true },
                containerColor = Azul500,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova OS")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = termoBusca,
                onValueChange = { termoBusca = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por título...") },
                singleLine = true
            )

            SectionHeader("Filtrar por status")
            FiltroChips(
                opcoes = listOf("todas" to "Todas", "pendente" to "Pendentes", "concluida" to "Concluídas"),
                selecionado = uiState.filtroStatus,
                onSelect = { viewModel.setFiltroStatus(it) }
            )

            val ordens = viewModel.ordensFiltradas().filter {
                termoBusca.isBlank() || it.titulo.contains(termoBusca, ignoreCase = true)
            }

            if (ordens.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma ordem encontrada.\nClique no + para criar uma nova.")
                }
            } else {
                LazyColumn {
                    items(ordens, key = { it.id }) { os ->
                        OSCard(os = os, onClick = { onOSClick(os) }, mostrarFuncionario = true)
                    }
                }
            }
        }
    }

    if (showNovaOS) {
        NovaOSSheet(
            funcionarios = uiState.funcionarios,
            isLoading = uiState.isLoading,
            onDismiss = { showNovaOS = false },
            onEnviar = { titulo, descricao, dept, func ->
                viewModel.criarOrdem(titulo, descricao, dept, func)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaOSSheet(
    funcionarios: List<User>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onEnviar: (String, String, String, User) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var departamento by remember { mutableStateOf("") }
    var funcionarioSelecionado by remember { mutableStateOf<User?>(null) }
    var expandedDept by remember { mutableStateOf(false) }
    var expandedFunc by remember { mutableStateOf(false) }

    val departamentos = listOf("Manutenção", "TI", "Limpeza", "Segurança", "Administrativo")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Nova Ordem de Serviço", fontWeight = FontWeight.Medium, fontSize = 17.sp)

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expandedDept,
                onExpandedChange = { expandedDept = it }
            ) {
                OutlinedTextField(
                    value = departamento,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Departamento") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDept) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedDept, onDismissRequest = { expandedDept = false }) {
                    departamentos.forEach { dept ->
                        DropdownMenuItem(
                            text = { Text(dept) },
                            onClick = { departamento = dept; expandedDept = false }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedFunc,
                onExpandedChange = { expandedFunc = it }
            ) {
                OutlinedTextField(
                    value = funcionarioSelecionado?.nome ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Funcionário") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFunc) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedFunc, onDismissRequest = { expandedFunc = false }) {
                    funcionarios.forEach { func ->
                        DropdownMenuItem(
                            text = { Text(func.nome) },
                            onClick = { funcionarioSelecionado = func; expandedFunc = false }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    funcionarioSelecionado?.let {
                        onEnviar(titulo, descricao, departamento, it)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isLoading && funcionarioSelecionado != null && titulo.isNotBlank() && descricao.isNotBlank() && departamento.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Enviar OS")
            }
        }
    }
}