package com.msa.chatlab.core.common.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * ✅ Dedup: canonical ProfileCard lives in core-designsystem.
 * NOTE: This package name is singular (component). Keep for backward-compat.
 */
@Deprecated(
    message = "Use com.msa.chatlab.core.designsystem.component.ProfileCard",
    replaceWith = ReplaceWith(
        "ProfileCard(name, protocol, endpoint, isActive, onClick, onMenuClick, modifier)",
        "com.msa.chatlab.core.designsystem.component.ProfileCard"
    )
)
@Composable
fun ProfileCard(
    name: String,
    protocol: String,
    endpoint: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) = com.msa.chatlab.core.designsystem.component.ProfileCard(
    name = name,
    protocol = protocol,
    endpoint = endpoint,
    isActive = isActive,
    onClick = onClick,
    onMenuClick = onMenuClick,
    modifier = modifier
)
