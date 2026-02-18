package com.msa.chatlab.core.data.lab

import com.msa.chatlab.core.storage.dao.EventDao
import com.msa.chatlab.core.storage.dao.RunDao
import com.msa.chatlab.core.storage.entity.EventEntity
import com.msa.chatlab.core.storage.entity.RunEntity
import kotlinx.coroutines.flow.Flow

class RoomRunRepository(
    private val runDao: RunDao,
    private val eventDao: EventDao
) : RunRepository {

    override fun observeRecentRuns(limit: Int): Flow<List<RunEntity>> = runDao.observeRecent(limit)

    override fun observeEvents(runId: String): Flow<List<EventEntity>> = eventDao.observeByRun(runId)

    override suspend fun upsertRun(run: RunEntity) = runDao.upsert(run)

    override suspend fun insertEvents(events: List<EventEntity>) = eventDao.insertAll(events)

    override suspend fun getRun(runId: String): RunEntity? = runDao.getById(runId)
}
