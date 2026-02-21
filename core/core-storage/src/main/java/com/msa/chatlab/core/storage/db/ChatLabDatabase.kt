package com.msa.chatlab.core.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.msa.chatlab.core.storage.dao.EventDao
import com.msa.chatlab.core.storage.dao.MessageDao
import com.msa.chatlab.core.storage.dao.OutboxDao
import com.msa.chatlab.core.storage.dao.PresetDao
import com.msa.chatlab.core.storage.dao.ProfileDao
import com.msa.chatlab.core.storage.dao.RunDao
import com.msa.chatlab.core.storage.db.converters.Converters
import com.msa.chatlab.core.domain.model.MessageEntity
import com.msa.chatlab.core.storage.entity.EventEntity
import com.msa.chatlab.core.storage.entity.OutboxItemEntity
import com.msa.chatlab.core.storage.entity.PresetEntity
import com.msa.chatlab.core.storage.entity.ProfileEntity
import com.msa.chatlab.core.storage.entity.RunEntity

@TypeConverters(Converters::class)
@Database(
    entities = [
        MessageEntity::class,
        OutboxItemEntity::class,
        ProfileEntity::class,
        RunEntity::class,
        EventEntity::class,
        PresetEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ChatLabDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun outboxDao(): OutboxDao
    abstract fun profileDao(): ProfileDao
    abstract fun runDao(): RunDao
    abstract fun eventDao(): EventDao
    abstract fun presetDao(): PresetDao

    companion object {
        const val DATABASE_NAME = "chatlab.db"
    }
}
