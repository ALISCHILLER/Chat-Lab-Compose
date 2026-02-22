package com.msa.chatlab.core.data.dedup

import com.msa.chatlab.core.storage.dao.DedupDao
import com.msa.chatlab.core.storage.entity.DedupEntity

class RoomDedupStore(private val dedupDao: DedupDao) : DedupStore {
    override suspend fun shouldProcess(key: String): Boolean {
        val existing = dedupDao.getById(key)
        return if (existing == null) {
            dedupDao.insert(DedupEntity(key, System.currentTimeMillis()))
            true
        } else {
            false
        }
    }

    override suspend fun prune() {
        // This is handled by a separate worker/job
    }

    override suspend fun clear() {
        // Not implemented for this persistent store
    }
}
