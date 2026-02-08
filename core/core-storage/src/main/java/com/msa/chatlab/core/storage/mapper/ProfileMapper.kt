package com.msa.chatlab.core.storage.mapper

import com.msa.chatlab.core.domain.model.Profile
import com.msa.chatlab.core.storage.entity.ProfileEntity

object ProfileMapper {

    fun toEntity(profile: Profile, profileJson: String, now: Long): ProfileEntity {
        return ProfileEntity(
            id = profile.id.value,
            name = profile.name,
            description = profile.description,
            tagsCsv = profile.tags.joinToString(","),
            protocolType = profile.protocolType.name,
            profileJson = profileJson,
            createdAt = now,
            updatedAt = now
        )
    }

    /**
     * Domain واقعی را هنوز اینجا نمی‌سازیم چون JSON decoder نداریم.
     * در core-data با codec استاندارد انجام می‌دهیم.
     */
    fun toRawJson(entity: ProfileEntity): String = entity.profileJson
}
