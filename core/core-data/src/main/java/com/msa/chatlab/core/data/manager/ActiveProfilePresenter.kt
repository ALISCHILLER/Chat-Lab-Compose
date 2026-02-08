package com.msa.chatlab.core.data.manager

import com.msa.chatlab.core.data.active.ActiveProfileStore
import com.msa.chatlab.core.domain.model.Profile
import kotlinx.coroutines.flow.Flow

class ActiveProfilePresenter(
    private val store: ActiveProfileStore
) {
    val active: Flow<Profile?> = store.activeProfile
}
