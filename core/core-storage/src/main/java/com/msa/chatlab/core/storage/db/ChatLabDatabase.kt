package com.msa.chatlab.core.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.msa.chatlab.core.storage.dao.EventDao
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.storage.dao.ProfileDao
import com.msa.chatlab.core.storage.dao.RunDao
import com.msa.chatlab.core.storage.entity.EventEntity
import com.msa.chatlab.core.storage.entity.MessageEntity
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import com.msa.chatlab.core.storage.entity.ProfileEntity
import com.msa.chatlab.core.storage.entity.RunEntity

@Database(
    entities = [
        ProfileEntity::class, 
        RunEntity::class, 
        EventEntity::class, 
        MessageEntity::class, 
        OutboxItemEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class ChatLabDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun runDao(): RunDao
    abstract fun eventDao(): EventDao
    abstract fun messageDao(): MessageDao
    abstract fun outboxDao(): OutboxDao

    companion object {
        const val DATABASE_NAME = "chatlab.db"
    }
}
