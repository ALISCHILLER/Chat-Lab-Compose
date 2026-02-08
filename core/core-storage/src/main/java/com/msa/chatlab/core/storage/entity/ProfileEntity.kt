package com.msa.chatlab.core.storage.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profiles",
    indices = [
        Index(value = ["name"]),
        Index(value = ["protocolType"]),
        Index(value = ["updatedAt"])
    ]
)
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val tagsCsv: String,        // برای سرچ ساده
    val protocolType: String,   // enum name
    val profileJson: String,    // کل پروفایل به صورت JSON خام (فعلاً)
    val createdAt: Long,
    val updatedAt: Long
)
