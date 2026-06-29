package com.example.serviceflow.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "",
    val nome: String = "",
    val email: String = "",
    val tipo: String = "funcionario",
    val departamento: String = ""
)

@Entity(tableName = "ordens_servico")
data class OrdemServico(
    @PrimaryKey val id: String = "",
    val numero: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val dataCriacao: Timestamp = Timestamp.now(),
    val departamento: String = "",
    val funcionarioId: String = "",
    val funcionarioNome: String = "",
    val status: String = "pendente",
    val realizadoPor: String = "",
    val dataConclusao: Timestamp? = null
)

enum class StatusOS { PENDENTE, CONCLUIDA }
enum class TipoUsuario { ADMIN, FUNCIONARIO }
