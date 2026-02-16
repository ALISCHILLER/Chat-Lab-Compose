package com.msa.chatlab.core.storage.db.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.msa.chatlab.core.domain.model.Profile

class Converters {
    @TypeConverter
    fun fromProfile(profile: Profile?): String? {
        return Gson().toJson(profile)
    }

    @TypeConverter
    fun toProfile(profileString: String?): Profile? {
        return Gson().fromJson(profileString, Profile::class.java)
    }
}