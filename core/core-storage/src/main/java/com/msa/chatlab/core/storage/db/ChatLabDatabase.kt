package com.msa.chatlab.core.storage.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.msa.chatlab.core.storage.dao.*
import com.msa.chatlab.core.storage.db.converters.Converters
import com.msa.chatlab.core.storage.entity.*

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