package com.msa.chatlab.core.storage.db.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.storage.entity.OutboxStatus

class Converters {

    // (فعلاً لازم نیست ولی نگه می‌داریم)
    @TypeConverter
    fun fromProfile(profile: Profile?): String? = Gson().toJson(profile)

    @TypeConverter
    fun toProfile(profileString: String?): Profile? =
        Gson().fromJson(profileString, Profile::class.java)

    // ✅ OutboxStatus as TEXT
    @TypeConverter
    fun fromOutboxStatus(v: OutboxStatus?): String? = v?.name

    @TypeConverter
    fun toOutboxStatus(v: String?): OutboxStatus? =
        v?.let { runCatching { OutboxStatus.valueOf(it) }.getOrNull() }
}
