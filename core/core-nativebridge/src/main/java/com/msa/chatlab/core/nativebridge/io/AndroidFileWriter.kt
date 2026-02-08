package com.msa.chatlab.core.nativebridge.io

import java.io.File

class AndroidFileWriter : FileWriter {
    override fun writeText(file: File, text: String) {
        file.parentFile?.mkdirs()
        file.writeText(text)
    }
}
