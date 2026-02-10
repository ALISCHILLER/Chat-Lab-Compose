package com.msa.chatlab.featurelab.route

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.msa.chatlab.featurelab.screen.LabScreen
import com.msa.chatlab.featurelab.vm.LabViewModel

@Composable
fun LabRoute() {
    val vm: LabViewModel = viewModel()
    LabScreen(vm = vm)
}
