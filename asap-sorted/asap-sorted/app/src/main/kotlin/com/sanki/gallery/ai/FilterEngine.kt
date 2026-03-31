package com.sanki.gallery.ai

import com.sanki.gallery.core.ScannedFile
import java.io.FileInputStream
import java.security.MessageDigest

object FilterEngine {
    
    // MD5 hashing to quickly catch duplicate memes/images
    fun generateFileHash(file: ScannedFile): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val javaFile = java.io.File(file.absolutePath)
            if (javaFile.exists() && javaFile.canRead()) {
                FileInputStream(javaFile).use { fis ->
                    val buffer = ByteArray(128 * 1024) // 128KB chunk
                    val bytesRead = fis.read(buffer)
                    if (bytesRead > 0) {
                        md.update(buffer, 0, bytesRead)
                    }
                }
            } else {
                md.update(file.absolutePath.toByteArray()) 
            }
            val bytes = md.digest() 
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "error_hash_${file.absolutePath}"
        }
    }

    // Metadata rules (e.g. 50% work done over metadata)
    fun analyzeMetadata(file: ScannedFile): Int? {
        val path = file.absolutePath.lowercase()
        
        // --- 100% Importance Rules ---
        if (path.contains("whatsapp documents")) {
            return 100 
        }
        if (path.contains("download") && file.extension == "pdf") {
            return 100
        }
        
        // --- 100% Waste/Bloat Rules ---
        val wasteFolders = listOf(
            "whatsapp voice notes", 
            ".statuses", 
            "/sent", 
            ".thumbnails", 
            "/cache", 
            "/trash",
            "whatsapp animated gifs"
        )
        if (wasteFolders.any { path.contains(it) }) {
            return 0 
        }
        
        // Exact global spam matching concept
        if (GlobalSpamDB.contains(generateFileHash(file))) {
            return 0 
        }

        return null // Let AI decide
    }
}

// Mocking the Global pHash Spam database
object GlobalSpamDB {
    private val spamHashes = setOf("badhash1", "badhash2")
    fun contains(hash: String) = spamHashes.contains(hash)
}
