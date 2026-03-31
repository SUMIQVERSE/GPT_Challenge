package com.sanki.gallery.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sanki.gallery.ai.AIAnalyzer
import com.sanki.gallery.ai.FilterEngine
import com.sanki.gallery.db.AppDatabase
import com.sanki.gallery.db.ScannedFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ScanWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ScanWorker", "Starting Eco-Scan memory-safe chunk process...")
        val scanner = StorageScanner()
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.scannedFileDao()
        val aiAnalyzer = AIAnalyzer(applicationContext)

        return withContext(Dispatchers.IO) {
            try {
                var processedCount = 0

                // Fetch trained weights once before scan
                val weightsList = db.aiLearningWordDao().getAllWeights()
                val wordWeights = weightsList.associate { it.word to it.weight }
                
                // Fetch trained signature weights
                val sigList = db.aiSignatureDao().getAllWeights()
                val signatureWeights = sigList.associate { it.signature to it.weight }
                
                Log.d("ScanWorker", "Loaded ${wordWeights.size} word weights and ${signatureWeights.size} signature weights.")

                scanner.scanDirectory().collect { scannedFile ->

                    // --- LAYER 0: Waste Folder Bypass (Fastest, zero AI) ---
                    if (scanner.isWasteFolder(scannedFile.absolutePath)) {
                        dao.insert(
                            ScannedFileEntity(
                                absolutePath = scannedFile.absolutePath,
                                name = scannedFile.name,
                                sizeBytes = scannedFile.sizeBytes,
                                extension = scannedFile.extension,
                                fileHash = FilterEngine.generateFileHash(scannedFile),
                                aiScore = 0,
                                tags = "waste_folder"
                            )
                        )
                        processedCount++
                        return@collect
                    }

                    // --- LAYER 1: Duplicate Hash Check ---
                    val fileHash = FilterEngine.generateFileHash(scannedFile)
                    val existingEntry = dao.getByHash(fileHash)

                    if (existingEntry != null && existingEntry.absolutePath != scannedFile.absolutePath) {
                        dao.insert(
                            ScannedFileEntity(
                                absolutePath = scannedFile.absolutePath,
                                name = scannedFile.name,
                                sizeBytes = scannedFile.sizeBytes,
                                extension = scannedFile.extension,
                                fileHash = fileHash,
                                aiScore = 5,
                                tags = "exact_duplicate"
                            )
                        )
                        processedCount++
                        return@collect
                    }

                    // --- LAYER 2: Metadata Path Rules ---
                    val metadataScore = FilterEngine.analyzeMetadata(scannedFile)
                    if (metadataScore != null) {
                        dao.insert(
                            ScannedFileEntity(
                                absolutePath = scannedFile.absolutePath,
                                name = scannedFile.name,
                                sizeBytes = scannedFile.sizeBytes,
                                extension = scannedFile.extension,
                                fileHash = fileHash,
                                aiScore = metadataScore,
                                tags = if (metadataScore >= 80) "important,doc" else "metadata_bloat"
                            )
                        )
                        processedCount++
                        return@collect
                    }

                    // --- LAYER 3: On-Device AI (ML Kit OCR) ---
                    val ext = scannedFile.extension.lowercase()
                    val isImage = ext in setOf("jpg", "jpeg", "png", "webp", "gif")
                    val isPdf = ext == "pdf"

                    val (finalScore, tags, textCache, signature) = if (isImage) {
                        val options = BitmapFactory.Options().apply {
                            inSampleSize = 4 // Downsample 4x to save RAM
                            inJustDecodeBounds = false
                        }
                        val bitmap = BitmapFactory.decodeFile(scannedFile.absolutePath, options)
                        if (bitmap != null) {
                            val result = aiAnalyzer.analyze(bitmap, scannedFile.absolutePath, wordWeights, signatureWeights)
                            bitmap.recycle()
                            // Quadruple holding score, tags, text, signature
                            listOf(result.score, result.tags.joinToString(","), result.textCache, result.signature)
                        } else {
                            listOf(40, "unreadable", null, null)
                        }
                    } else if (isPdf) {
                        try {
                            val file = File(scannedFile.absolutePath)
                            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                            val renderer = PdfRenderer(fd)
                            if (renderer.pageCount > 0) {
                                val page = renderer.openPage(0)
                                // We render a bitmap large enough for ML Kit to read comfortably, but keeping RAM low (e.g. 800x1200)
                                val bmp = Bitmap.createBitmap(800, 1131, Bitmap.Config.ARGB_8888)
                                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                val result = aiAnalyzer.analyze(bmp, scannedFile.absolutePath, wordWeights, signatureWeights)
                                page.close()
                                renderer.close()
                                fd.close()
                                bmp.recycle()
                                listOf(result.score, result.tags.joinToString(",") + ",pdf_doc", result.textCache, result.signature)
                            } else {
                                renderer.close()
                                fd.close()
                                listOf(60, "empty_pdf", null, null)
                            }
                        } catch (e: Exception) {
                            Log.e("ScanWorker", "Failed to render PDF: ${scannedFile.absolutePath}", e)
                            listOf(50, "pdf_render_error", null, null)
                        }
                    } else {
                        // Non-image files get a neutral score
                        listOf(60, "media_file", null, null)
                    }

                    dao.insert(
                        ScannedFileEntity(
                            absolutePath = scannedFile.absolutePath,
                            name = scannedFile.name,
                            sizeBytes = scannedFile.sizeBytes,
                            extension = scannedFile.extension,
                            fileHash = fileHash,
                            aiScore = finalScore as Int,
                            tags = tags as String,
                            textCache = textCache as String?,
                            signature = signature as String?
                        )
                    )

                    processedCount++

                    // GC every 20 files to prevent OOM on 4GB RAM devices
                    if (processedCount % 20 == 0) {
                        System.gc()
                        Log.d("ScanWorker", "Processed $processedCount files")
                    }
                }

                Log.d("ScanWorker", "Scan complete! Total: $processedCount files")
                Result.success()
            } catch (e: Exception) {
                Log.e("ScanWorker", "Error in scan processing", e)
                Result.failure()
            }
        }
    }
}
