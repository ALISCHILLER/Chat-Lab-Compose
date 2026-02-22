package com.msa.chatlab.core.common.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * A shared, application-level CoroutineScope.
 *
 * This scope should be used for long-running tasks that need to outlive specific screens
 * or view models. The scope is tied to the application's lifecycle.
 *
 * Using a SupervisorJob means that if one child coroutine fails, it won't cancel the
 * entire scope.
 */
interface AppScope {
    val scope: CoroutineScope
}

class DefaultAppScope(
    coroutineDispatcher: CoroutineDispatcher
) : AppScope {
    override val scope: CoroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatcher)
}
