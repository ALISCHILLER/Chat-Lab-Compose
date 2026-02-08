package com.msa.chatlab.di

import com.msa.chatlab.core.data.lab.*
import com.msa.chatlab.featurelab.vm.LabViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val LabModule = module {
    // Scope برای اجرای سناریو
    single { CoroutineScope(Dispatchers.Default) }

    // اجزای آزمایشگاه
    single { ScenarioExecutor(get(), get(), get(), get()) }
    single { SessionExporter(get(), get()) }

    // ViewModel
    viewModel {
        LabViewModel(
            profileManager = get(),
            scenarioExecutor = get(),
            sessionExporter = get()
        )
    }
}
