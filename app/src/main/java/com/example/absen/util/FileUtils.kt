package com.example.absen.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun getFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("cuti_doc", ".pdf", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)

        inputStream?.copyTo(outputStream)

        inputStream?.close()
        outputStream.close()

        return tempFile
    }
}