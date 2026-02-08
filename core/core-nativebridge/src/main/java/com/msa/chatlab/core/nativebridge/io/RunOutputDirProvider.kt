package com.msa.chatlab.core.nativebridge.io

import java.io.File

interface RunOutputDirProvider {
    fun dirForRun(runId: String): File
}
