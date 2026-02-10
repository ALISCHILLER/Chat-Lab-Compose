package com.msa.chatlab.core.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val tagsCsv: String,
    val protocolType: String,
    val profileJson: String,
    val createdAt: Long,
    val updatedAt: Long
)
