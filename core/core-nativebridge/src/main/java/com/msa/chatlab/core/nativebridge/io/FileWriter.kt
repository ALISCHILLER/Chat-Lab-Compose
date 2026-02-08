package com.msa.chatlab.core.nativebridge.io

import java.io.File

interface FileWriter {
    fun writeText(file: File, text: String)
}
