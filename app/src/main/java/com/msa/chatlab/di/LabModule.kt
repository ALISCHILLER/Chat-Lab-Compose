package com.msa.chatlab.di

import com.msa.chatlab.feature.lab.vm.LabViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val LabModule = module {
    viewModel { LabViewModel(get(), get(), get(), get()) }
}
