package com.example.serviceflow.di

import androidx.room.Room
import com.example.serviceflow.data.local.AppDatabase
import com.example.serviceflow.repository.ServiceFlowRepository
import com.example.serviceflow.viewmodel.AdminViewModel
import com.example.serviceflow.viewmodel.AuthViewModel
import com.example.serviceflow.viewmodel.FuncionarioViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "serviceflow_db"
        ).build()
    }
    single { get<AppDatabase>().ordemDao() }
    single { ServiceFlowRepository(get()) }
    viewModel { AdminViewModel(get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { FuncionarioViewModel(get()) }
}
