package com.msa.chatlab.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val AppCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
