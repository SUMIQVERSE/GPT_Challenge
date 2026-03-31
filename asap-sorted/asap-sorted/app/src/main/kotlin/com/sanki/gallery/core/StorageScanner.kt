package com.sanki.gallery.core

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

data class ScannedFile(
    val absolutePath: String,
    val name: String,
    val sizeBytes: Long,
    val extension: String
)

/**
 * Scans all accessible directories on the device.
 * Uses standard File walking — works without root/Shizuku on all accessible paths.
 */
class StorageScanner {

    private val validExtensions = setOf(
        "jpg", "jpeg", "png", "webp", "gif",
        "mp4", "3gp", "mkv", "avi",
        "pdf", "docx", "doc", "xlsx", "pptx",
        "mp3", "aac", "ogg", "wav"
    )

    // Folders that are pure bloat — skip AI entirely
    private val wasteFolderKeywords = listOf(
        ".thumbnails", ".trash", "/.cache", "/cache",
        "whatsapp/media/.statuses", "whatsapp/media/whatsapp voice notes",
        "whatsapp/media/whatsapp animated gifs",
        "whatsapp/media/whatsapp video notes",
        "whatsapp/sent"
    )

    fun scanDirectory(rootPath: String = "/storage/emulated/0"): Flow<ScannedFile> = flow {
        val root = java.io.File(rootPath)
        if (!root.exists() || !root.canRead()) {
            Log.e("StorageScanner", "Cannot read root path: $rootPath")
            return@flow
        }

        Log.d("StorageScanner", "Starting scan from: $rootPath")
        var count = 0

        root.walkTopDown()
            .onEnter { dir ->
                // Skip Android system dirs we can't read anyway
                val path = dir.absolutePath.lowercase()
                !path.contains("/android/data") && !path.contains("/.android")
            }
            .forEach { file ->
                try {
                    if (file.isFile && file.canRead()) {
                        val ext = file.extension.lowercase()
                        if (ext in validExtensions && file.length() > 1024) { // Skip tiny files < 1KB
                            emit(
                                ScannedFile(
                                    absolutePath = file.absolutePath,
                                    name = file.name,
                                    sizeBytes = file.length(),
                                    extension = ext
                                )
                            )
                            count++
                            if (count % 100 == 0) {
                                Log.d("StorageScanner", "Scanned $count files so far...")
                                System.gc()
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Skip unreadable files silently
                }
            }

        Log.d("StorageScanner", "Scan complete. Total files: $count")
    }.flowOn(Dispatchers.IO)

    fun isWasteFolder(path: String): Boolean {
        val lower = path.lowercase()
        return wasteFolderKeywords.any { lower.contains(it) }
    }
}
