package com.sanki.gallery.ai

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import com.runanywhere.sdk.foundation.bridge.extensions.CppBridgeModelPaths
import com.runanywhere.sdk.llm.llamacpp.LlamaCPP
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.SDKEnvironment
import com.runanywhere.sdk.public.extensions.Models.ModelCategory
import com.runanywhere.sdk.public.extensions.Models.ModelFileDescriptor
import com.runanywhere.sdk.public.extensions.registerMultiFileModel
import com.runanywhere.sdk.storage.AndroidPlatformContext
import com.sanki.gallery.db.AppDatabase
import com.sanki.gallery.db.ScannedFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import com.runanywhere.sdk.core.types.InferenceFramework

/**
 * DeepSearchEngine — The "Chat with Gallery" Brain
 * 
 * This singleton wraps RunAnywhere SDK for on-device VLM (Vision Language Model).
 * It is LAZY — model loads ONLY when user opens Chat with Gallery screen,
 * and unloads immediately when they leave. Zero RAM cost during normal scanning.
 *
 * Uses SmolVLM-256M-Instruct — the smallest multimodal model available:
 * - ~256MB download (one-time WiFi)
 * - ~365MB RAM when loaded
 * - Fully offline after download
 * - Can "see" and "describe" images in natural language
 */
object DeepSearchEngine {

    private const val TAG = "DeepSearchEngine"

    // SmolVLM 256M — smallest VLM, perfect for mobile
    private const val VLM_MODEL_ID = "smolvlm-256m-instruct"
    private const val VLM_MODEL_NAME = "SmolVLM 256M Instruct"
    private const val VLM_MODEL_URL = "https://huggingface.co/RunanywhereAI/SmolVLM-256M-Instruct-GGUF/resolve/main/SmolVLM-256M-Instruct-Q8_0.gguf"
    private const val VLM_PROJ_URL = "https://huggingface.co/RunanywhereAI/SmolVLM-256M-Instruct-GGUF/resolve/main/mmproj-SmolVLM-256M-Instruct-f16.gguf"
    private const val VLM_MEMORY_REQ = 365_000_000L // 365MB

    private var sdkInitialized = false
    private var modelRegistered = false

    // ── SDK Init (call once from Application.onCreate) ──────────────────────
    fun initializeSDK(context: Context) {
        if (sdkInitialized) return
        try {
            AndroidPlatformContext.initialize(context)
            RunAnywhere.initialize(environment = SDKEnvironment.PRODUCTION)

            val modelsDir = context.filesDir.resolve("runanywhere").also { it.mkdirs() }
            CppBridgeModelPaths.setBaseDirectory(modelsDir.absolutePath)

            // Register LlamaCPP backend (VLM support)
            try {
                LlamaCPP.register(priority = 100)
            } catch (e: Exception) {
                Log.w(TAG, "LlamaCPP VLM registration failed (LLM still works): ${e.message}")
            }

            sdkInitialized = true
            Log.d(TAG, "RunAnywhere SDK initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "SDK initialization failed", e)
        }
    }

    // ── Register Model (idempotent) ─────────────────────────────────────────
    private fun ensureModelRegistered() {
        if (modelRegistered) return
        try {
            RunAnywhere.registerMultiFileModel(
                id = VLM_MODEL_ID,
                name = VLM_MODEL_NAME,
                files = listOf(
                    ModelFileDescriptor(url = VLM_MODEL_URL, filename = "SmolVLM-256M-Instruct-Q8_0.gguf"),
                    ModelFileDescriptor(url = VLM_PROJ_URL, filename = "mmproj-SmolVLM-256M-Instruct-f16.gguf")
                ),
                framework = InferenceFramework.LLAMA_CPP,
                modality = ModelCategory.MULTIMODAL,
                memoryRequirement = VLM_MEMORY_REQ
            )
            modelRegistered = true
            Log.d(TAG, "VLM model registered")
        } catch (e: Exception) {
            Log.e(TAG, "Model registration failed", e)
        }
    }

    // ── Check if model files are downloaded ─────────────────────────────────
    fun isModelDownloaded(): Boolean {
        ensureModelRegistered()
        return try {
            RunAnywhere.isModelDownloaded(VLM_MODEL_ID)
        } catch (e: Exception) {
            false
        }
    }

    // ── Download model (returns progress Flow) ──────────────────────────────
    fun downloadModel(): Flow<Float> = flow {
        ensureModelRegistered()
        RunAnywhere.downloadModel(VLM_MODEL_ID).collect { progress ->
            emit(progress.progress)
        }
    }.flowOn(Dispatchers.IO)

    // ── Load into RAM (call when user opens Deep Search) ────────────────────
    suspend fun loadModel(): Boolean = withContext(Dispatchers.IO) {
        ensureModelRegistered()
        try {
            RunAnywhere.loadVLMModel(VLM_MODEL_ID)
            Log.d(TAG, "VLM model loaded into RAM")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load VLM model", e)
            false
        }
    }

    // ── Unload from RAM (call when user leaves Deep Search) ─────────────────
    suspend fun unloadModel() = withContext(Dispatchers.IO) {
        try {
            RunAnywhere.unloadLLMModel()
            Log.d(TAG, "VLM model unloaded from RAM")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unload VLM model (may not have been loaded)", e)
        }
    }

    // ── Check if model is currently loaded ──────────────────────────────────
    fun isModelLoaded(): Boolean {
        return try {
            RunAnywhere.isVLMModelLoaded
        } catch (e: Exception) {
            false
        }
    }

    // ── Check device RAM before loading ─────────────────────────────────────
    fun hasEnoughRAM(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val available = memInfo.availMem
        // Need at least 1.5x the model memory requirement to be safe
        return available > (VLM_MEMORY_REQ * 1.5)
    }

    // ── Describe a single image using VLM ───────────────────────────────────
    /**
     * VLM looks at the image and generates a text description.
     * Streams tokens for real-time UX.
     */
    fun describeImage(imagePath: String, userPrompt: String = "Describe this image in detail. What objects, people, text, colors, and context do you see?"): Flow<String> = flow {
        if (!isModelLoaded()) {
            emit("[Error: AI model not loaded. Please wait for loading to complete.]")
            return@flow
        }

        try {
            // VLM needs file path workaround — copy to cache if needed
            val tempFile = File.createTempFile("vlm_", ".jpg")
            val file = File(imagePath)
            val ext = file.extension.lowercase()

            if (ext == "pdf") {
                // Render PDF first page as image for VLM
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fd)
                if (renderer.pageCount > 0) {
                    val page = renderer.openPage(0)
                    val bmp = Bitmap.createBitmap(1200, 1697, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, tempFile.outputStream())
                    page.close()
                    bmp.recycle()
                }
                renderer.close()
                fd.close()
            } else {
                // For images — downsample to save VLM processing time
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                val bmp = BitmapFactory.decodeFile(imagePath, opts)
                if (bmp != null) {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 85, tempFile.outputStream())
                    bmp.recycle()
                } else {
                    emit("[Error: Cannot decode image]")
                    return@flow
                }
            }

            // Stream VLM response
            RunAnywhere.generateStream(
                "$userPrompt\n[Image: ${tempFile.absolutePath}]"
            ).collect { token ->
                emit(token)
            }

            // Cleanup temp file
            tempFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "VLM describe failed", e)
            emit("[Error: ${e.message}]")
        }
    }.flowOn(Dispatchers.IO)

    // ── Search gallery using hybrid approach ────────────────────────────────
    /**
     * Phase 1 (Instant): Search textCache + tags + visionDescription in Room DB
     * Phase 2 (Deep): If Phase 1 returns < 3 results, VLM analyzes top candidates
     */
    suspend fun searchGallery(
        query: String,
        context: Context
    ): List<ScannedFileEntity> = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val dao = db.scannedFileDao()

        // Phase 1: Instant keyword search across cached text
        val keywords = query.lowercase().split(Regex("\\s+")).filter { it.length > 2 }
        val phase1Results = mutableListOf<ScannedFileEntity>()

        for (keyword in keywords) {
            val matches = dao.searchByText("%$keyword%")
            phase1Results.addAll(matches)
        }

        // Deduplicate and rank by how many keywords matched
        val ranked = phase1Results
            .groupBy { it.absolutePath }
            .mapValues { (_, v) -> v.first() to v.size }
            .values
            .sortedByDescending { it.second }
            .map { it.first }

        if (ranked.size >= 3) {
            Log.d(TAG, "Phase 1 found ${ranked.size} results for: $query")
            return@withContext ranked.take(50)
        }

        // Phase 2: VLM deep search on unsure/untagged candidates
        if (!isModelLoaded()) {
            Log.d(TAG, "VLM not loaded, returning Phase 1 results only")
            return@withContext ranked
        }

        Log.d(TAG, "Phase 1 insufficient (${ranked.size}), starting VLM Phase 2...")
        val candidates = dao.getUndescribedImages(20)
        val deepResults = mutableListOf<ScannedFileEntity>()

        for (candidate in candidates) {
            try {
                val description = StringBuilder()
                describeImage(candidate.absolutePath, "Briefly describe this image in 2-3 sentences.").collect { token ->
                    description.append(token)
                }

                val desc = description.toString().lowercase()
                // Cache the description for future instant searches
                dao.updateVisionDescription(candidate.absolutePath, desc)

                // Check if description matches query
                if (keywords.any { desc.contains(it) }) {
                    deepResults.add(candidate.copy(visionDescription = desc))
                }
            } catch (e: Exception) {
                Log.e(TAG, "VLM analysis failed for ${candidate.name}", e)
            }
        }

        return@withContext (ranked + deepResults).distinctBy { it.absolutePath }
    }
}
