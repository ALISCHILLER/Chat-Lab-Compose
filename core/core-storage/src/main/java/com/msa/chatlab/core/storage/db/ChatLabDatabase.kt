package com.msa.chatlab.core.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.storage.dao.ProfileDao
import com.msa.chatlab.core.storage.db.converters.Converters
import com.msa.chatlab.core.storage.entity.MessageEntity
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import com.msa.chatlab.core.storage.entity.ProfileEntity

@Database(
    entities = [MessageEntity::class, OutboxItemEntity::class, ProfileEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChatLabDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun outboxDao(): OutboxDao
    abstract fun profileDao(): ProfileDao

    companion object {
        const val DATABASE_NAME = "chatlab.db"
    }
}
