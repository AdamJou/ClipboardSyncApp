package com.amichon.clipboardsync

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File

class ClipboardHandler(
    private val context: Context,
    private val clipboardManager: ClipboardManager
) {

    fun updateClipboard(text: String) {
        val clip = ClipData.newPlainText("Received Text", text)
        clipboardManager.setPrimaryClip(clip)
        println("Clipboard updated on phone: $text")
    }

    fun updateClipboardWithImage(bitmap: Bitmap) {
        val file = saveImageToCache(bitmap)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val clip = ClipData.newUri(context.contentResolver, "Image", uri)
        clipboardManager.setPrimaryClip(clip)
        println("Clipboard updated with image.")
    }

    private fun saveImageToCache(bitmap: Bitmap): File {
        val file = File(context.cacheDir, "clipboard_image.png")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return file
    }
}
