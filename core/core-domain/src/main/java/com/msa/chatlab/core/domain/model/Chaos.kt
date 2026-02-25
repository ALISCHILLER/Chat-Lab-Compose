/*
 * Copyright 2023-2024 aklivity Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.msa.chatlab.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ChaosAction {

    @Serializable
    @SerialName("message.close")
    object MessageClose : ChaosAction()

    @Serializable
    @SerialName("message.reset")
    object MessageReset : ChaosAction()

    @Serializable
    @SerialName("message.abort")
    object MessageAbort : ChaosAction()

}

@Serializable
data class ChaosPolicy(
    val percentage: Int? = null,
    val actions: List<ChaosAction>? = null
)
