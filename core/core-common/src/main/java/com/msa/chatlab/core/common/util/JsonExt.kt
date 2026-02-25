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
package com.msa.chatlab.core.common.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun JsonObject.jStr(
    key: String
) = this[key]?.jStr ?: ""

fun JsonObject.jStrN(
    key: String
) = this[key]?.jStr

val JsonElement.jStr: String
    get() = when (this) {
        is JsonNull -> ""
        else -> this.jsonPrimitive.content
    }

fun JsonObject.jInt(
    key: String
) = this[key]?.jInt ?: 0

fun JsonObject.jIntN(
    key: String
) = this[key]?.jInt

val JsonElement.jInt: Int
    get() = when (this) {
        is JsonNull -> 0
        else -> this.jsonPrimitive.int
    }

fun JsonObject.jBool(
    key: String
) = this[key]?.jBool ?: false

val JsonElement.jBool: Boolean
    get() = when (this) {
        is JsonNull -> false
        else -> this.jsonPrimitive.boolean
    }

fun JsonObject.jObj(
    key: String
) = this[key]?.jObj

val JsonElement.jObj: JsonObject
    get() = this.jsonObject

fun JsonObject.jArray(
    key: String
) = this[key]?.jArray

val JsonElement.jArray: kotlinx.serialization.json.JsonArray
    get() = this.jsonArray
