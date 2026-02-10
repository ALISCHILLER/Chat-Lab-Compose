package com.msa.chatlab.core.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.storage.entity.MessageEntity
import com.msa.chatlab.core.storage.entity.OutboxItemEntity

@Database(
    entities = [MessageEntity::class, OutboxItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatLabDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun outboxDao(): OutboxDao

    companion object {
        const val DATABASE_NAME = "chatlab.db"
    }
}
