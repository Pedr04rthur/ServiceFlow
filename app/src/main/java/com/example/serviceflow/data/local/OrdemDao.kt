package com.example.serviceflow.data.local

import androidx.room.*
import com.example.serviceflow.model.OrdemServico
import com.example.serviceflow.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface OrdemDao {
    @Query("SELECT * FROM ordens_servico")
    fun getTodasOrdens(): Flow<List<OrdemServico>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrdens(ordens: List<OrdemServico>)

    @Query("SELECT * FROM users WHERE tipo = 'funcionario'")
    fun getFuncionarios(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)
}
