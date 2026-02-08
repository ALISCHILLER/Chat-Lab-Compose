package com.msa.chatlab.core.nativebridge.io

import android.content.Context
import java.io.File

class AndroidRunOutputDirProvider(
    private val context: Context
) : RunOutputDirProvider {
    override fun dirForRun(runId: String): File {
        // /data/data/<pkg>/files/lab_runs/<runId>/
        return File(File(context.filesDir, "lab_runs"), runId)
    }
}
