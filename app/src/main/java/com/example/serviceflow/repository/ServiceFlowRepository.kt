package com.example.serviceflow.repository

import android.util.Log
import com.example.serviceflow.model.OrdemServico
import com.example.serviceflow.model.User
import com.example.serviceflow.network.ApiService
import com.example.serviceflow.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp

class ServiceFlowRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val apiService: ApiService = RetrofitInstance.api

    fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
                return@AuthStateListener
            }

            Log.d("REPO", " Buscando usuário UID: ${firebaseUser.uid}")

            firestore.collection("users").document(firebaseUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nome = document.getString("nome") ?: firebaseUser.email ?: ""
                        val email = document.getString("email") ?: firebaseUser.email ?: ""
                        val tipo = document.getString("tipo") ?: "funcionario"
                        val departamento = document.getString("departamento") ?: ""

                        val user = User(
                            id = firebaseUser.uid,
                            nome = nome,
                            email = email,
                            tipo = tipo,
                            departamento = departamento
                        )
                        Log.d("REPO", " Usuário encontrado: ${user.nome}, tipo: ${user.tipo}")
                        trySend(user)
                    } else {
                        Log.e("REPO", " Documento NÃO encontrado para UID: ${firebaseUser.uid}")
                        trySend(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("REPO", " Erro ao buscar: ${e.message}")
                    trySend(null)
                }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun login(email: String, password: String): Result<User> = try {
        Log.d("REPO", " Login: $email")
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("Usuário não encontrado")

        val document = firestore.collection("users").document(firebaseUser.uid).get().await()

        val user = if (document.exists()) {
            val nome = document.getString("nome") ?: firebaseUser.email ?: ""
            val emailDoc = document.getString("email") ?: firebaseUser.email ?: ""
            val tipo = document.getString("tipo") ?: "funcionario"
            val departamento = document.getString("departamento") ?: ""

            User(
                id = firebaseUser.uid,
                nome = nome,
                email = emailDoc,
                tipo = tipo,
                departamento = departamento
            )
        } else {
            Log.e("REPO", " Documento não encontrado para UID: ${firebaseUser.uid}")
            User(
                id = firebaseUser.uid,
                nome = firebaseUser.email ?: "",
                email = firebaseUser.email ?: "",
                tipo = "funcionario",
                departamento = ""
            )
        }

        Log.d("REPO", " Login sucesso: ${user.nome}, tipo: ${user.tipo}")
        Result.success(user)
    } catch (e: Exception) {
        Log.e("REPO", " Login falhou: ${e.message}")
        Result.failure(e)
    }

    fun logout() {
        auth.signOut()
    }

    fun getTodasOrdens(): Flow<List<OrdemServico>> = callbackFlow {
        val subscription = firestore.collection("ordens")
            .orderBy("dataCriacao", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val ordens = snapshot?.documents?.mapNotNull { it.toObject<OrdemServico>()?.copy(id = it.id) } ?: emptyList()
                trySend(ordens)
            }
        awaitClose { subscription.remove() }
    }

    fun getOrdensDoFuncionario(funcionarioId: String): Flow<List<OrdemServico>> = callbackFlow {
        val subscription = firestore.collection("ordens")
            .whereEqualTo("funcionarioId", funcionarioId)
            .orderBy("dataCriacao", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val ordens = snapshot?.documents?.mapNotNull { it.toObject<OrdemServico>()?.copy(id = it.id) } ?: emptyList()
                trySend(ordens)
            }
        awaitClose { subscription.remove() }
    }

    fun getFuncionarios(): Flow<List<User>> = callbackFlow {
        val subscription = firestore.collection("users")
            .whereEqualTo("tipo", "funcionario")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val users = snapshot?.documents?.mapNotNull { it.toObject<User>()?.copy(id = it.id) } ?: emptyList()
                trySend(users)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun criarOrdem(titulo: String, descricao: String, departamento: String, funcionarioId: String, funcionarioNome: String): Result<Unit> = try {
        val numero = "OS-${System.currentTimeMillis()}"
        val ordem = OrdemServico(
            numero = numero,
            titulo = titulo,
            descricao = descricao,
            dataCriacao = Timestamp.now(),
            departamento = departamento,
            funcionarioId = funcionarioId,
            funcionarioNome = funcionarioNome,
            status = "pendente"
        )
        firestore.collection("ordens").add(ordem).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun concluirOrdem(ordemId: String, realizadoPor: String): Result<Unit> = try {
        val updates = mapOf(
            "status" to "concluida",
            "realizadoPor" to realizadoPor,
            "dataConclusao" to Timestamp.now()
        )
        firestore.collection("ordens").document(ordemId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}