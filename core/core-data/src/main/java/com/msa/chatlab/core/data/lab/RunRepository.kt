package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.storage.entity.EventEntity
import com.msa.chatlab.core.storage.entity.RunEntity
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    fun observeRecentRuns(limit: Int = 50): Flow<List<RunEntity>>
    fun observeEvents(runId: String): Flow<List<EventEntity>>

    suspend fun upsertRun(run: RunEntity)
    suspend fun insertEvents(events: List<EventEntity>)
    suspend fun getRun(runId: String): RunEntity?
}
