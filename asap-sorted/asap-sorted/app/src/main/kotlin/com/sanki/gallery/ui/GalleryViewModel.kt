package com.sanki.gallery.ui

import android.app.Application
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.sanki.gallery.ai.DeepSearchEngine
import com.sanki.gallery.db.AppDatabase
import com.sanki.gallery.db.ScannedFileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    object Done : ScanState()
    data class Error(val message: String) : ScanState()
}

// VLM Model lifecycle states
sealed class VLMModelState {
    object NotDownloaded : VLMModelState()
    data class Downloading(val progress: Float) : VLMModelState()
    object Downloaded : VLMModelState()
    object Loading : VLMModelState()
    object Ready : VLMModelState()
    data class Error(val message: String) : VLMModelState()
}

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.scannedFileDao()
    private val aiLearningDao = db.aiLearningWordDao()
    private val workManager = WorkManager.getInstance(application)

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _totalStorageBytes = MutableStateFlow(0L)
    val totalStorageBytes: StateFlow<Long> = _totalStorageBytes.asStateFlow()

    private val _usedStorageBytes = MutableStateFlow(0L)
    val usedStorageBytes: StateFlow<Long> = _usedStorageBytes.asStateFlow()

    val scannedFiles: StateFlow<List<ScannedFileEntity>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wasteFilesCount: StateFlow<Int> = dao.getAll()
        .map { list -> list.count { it.aiScore <= 25 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val wasteFilesSize: StateFlow<Long> = dao.getAll()
        .map { list -> list.filter { it.aiScore <= 25 }.sumOf { it.sizeBytes } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // ── VLM Deep Search States ──────────────────────────────────────────────
    private val _vlmModelState = MutableStateFlow<VLMModelState>(VLMModelState.NotDownloaded)
    val vlmModelState: StateFlow<VLMModelState> = _vlmModelState.asStateFlow()

    private val _deepSearchResults = MutableStateFlow<List<ScannedFileEntity>>(emptyList())
    val deepSearchResults: StateFlow<List<ScannedFileEntity>> = _deepSearchResults.asStateFlow()

    private val _deepSearchQuery = MutableStateFlow("")
    val deepSearchQuery: StateFlow<String> = _deepSearchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _vlmStreamText = MutableStateFlow("")
    val vlmStreamText: StateFlow<String> = _vlmStreamText.asStateFlow()

    private val _describingFile = MutableStateFlow<ScannedFileEntity?>(null)
    val describingFile: StateFlow<ScannedFileEntity?> = _describingFile.asStateFlow()

    init {
        loadRealStorageStats()
        checkVLMModelStatus()
    }

    private fun loadRealStorageStats() {
        viewModelScope.launch {
            try {
                val stat = StatFs(Environment.getExternalStorageDirectory().path)
                val blockSize = stat.blockSizeLong
                val total = stat.blockCountLong * blockSize
                val free = stat.availableBlocksLong * blockSize
                _totalStorageBytes.value = total
                _usedStorageBytes.value = total - free
            } catch (e: Exception) {
                _totalStorageBytes.value = 1L
                _usedStorageBytes.value = 0L
            }
        }
    }

    fun startScan() {
        _scanState.value = ScanState.Scanning
        viewModelScope.launch {
            try {
                dao.clearAll()
                val workRequest = OneTimeWorkRequestBuilder<com.sanki.gallery.core.ScanWorker>().build()
                workManager.enqueue(workRequest)

                workManager.getWorkInfoByIdLiveData(workRequest.id)
                    .observeForever { info ->
                        when (info?.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                _scanState.value = ScanState.Done
                                loadRealStorageStats()
                            }
                            WorkInfo.State.FAILED -> {
                                _scanState.value = ScanState.Error("Scan failed. Check storage permission.")
                            }
                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Mark a file as "Keep" — sets score to 80 so it moves to Important tab
    fun markKeep(file: ScannedFileEntity) {
        viewModelScope.launch {
            dao.insert(file.copy(aiScore = 80, tags = "user_kept"))
            processLearning(file.textCache, file.signature, isKeep = true)
        }
    }

    // Mark a file for deletion — rigorously deletes from both disk and MediaStore
    fun markDelete(file: ScannedFileEntity) {
        viewModelScope.launch {
            val cr = getApplication<android.app.Application>().contentResolver
            performPhysicalDelete(file.absolutePath, cr)
            // Always remove from DB regardless of physical deletion result
            dao.delete(file)
            processLearning(file.textCache, file.signature, isKeep = false)
        }
    }

    fun deleteWasteFiles() {
        viewModelScope.launch {
            try {
                val wasteFiles = dao.getWasteFiles()
                val cr = getApplication<android.app.Application>().contentResolver
                wasteFiles.forEach { fileEntity ->
                    performPhysicalDelete(fileEntity.absolutePath, cr)
                }
                dao.deleteWaste()
                loadRealStorageStats()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun performPhysicalDelete(absolutePath: String, cr: android.content.ContentResolver) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val javaFile = File(absolutePath)
            var physicallyDeleted = false
            
            // 1. Try standard Java IO deletion (works best with MANAGE_EXTERNAL_STORAGE)
            if (javaFile.exists()) {
                physicallyDeleted = javaFile.delete()
            } else {
                physicallyDeleted = true // already gone
            }

            // 2. We MUST remove it from MediaStore so the Gallery app instantly updates
            try {
                val uri = android.provider.MediaStore.Files.getContentUri("external")
                val selection = "${android.provider.MediaStore.Files.FileColumns.DATA} = ?"
                val selectionArgs = arrayOf(absolutePath)
                
                // First try to find its specific ID
                val cursor = cr.query(uri, arrayOf(android.provider.MediaStore.Files.FileColumns._ID), selection, selectionArgs, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val id = it.getLong(0)
                        val deleteUri = android.content.ContentUris.withAppendedId(uri, id)
                        cr.delete(deleteUri, null, null)
                        physicallyDeleted = true // Successfully yanked from system DB
                    } else {
                        // Not found by ID, try raw delete (less reliable but fallback)
                        if (!physicallyDeleted) {
                            try {
                                val rows = cr.delete(uri, selection, selectionArgs)
                                if (rows > 0) physicallyDeleted = true
                            } catch (e: Exception) { /* ignore */ }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetScan() {
        viewModelScope.launch {
            dao.clearAll()
            _scanState.value = ScanState.Idle
        }
    }

    // The Brain: Learns from both words and structural signatures
    private fun processLearning(text: String?, signature: String?, isKeep: Boolean) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            val wordDao = db.aiLearningWordDao()
            val sigDao = db.aiSignatureDao()
            
            // 1. Context Signature Learning
            if (signature != null) {
                if (isKeep) {
                    sigDao.incrementSignature(signature)
                } else {
                    sigDao.decrementSignature(signature)
                }
            }

            // 2. Word Learning
            if (!text.isNullOrBlank()) {
                val stopwords = setOf("the", "and", "for", "that", "this", "with", "from", "your", "have")
                val words = text.split(Regex("\\s+")).filter { it.length > 2 }
                for (word in words) {
                    val cleanWord = word.filter { it.isLetterOrDigit() }.lowercase()
                    if (cleanWord.length > 2 && cleanWord !in stopwords) {
                        if (isKeep) {
                            wordDao.incrementWeight(cleanWord)
                        } else {
                            wordDao.decrementWeight(cleanWord)
                        }
                    }
                }
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // VLM / DEEP SEARCH — Premium "Chat with Gallery" Feature
    // ═════════════════════════════════════════════════════════════════════════

    fun checkVLMModelStatus() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (DeepSearchEngine.isModelDownloaded()) {
                    _vlmModelState.value = VLMModelState.Downloaded
                } else {
                    _vlmModelState.value = VLMModelState.NotDownloaded
                }
            } catch (e: Exception) {
                _vlmModelState.value = VLMModelState.NotDownloaded
            }
        }
    }

    fun downloadVLMModel() {
        viewModelScope.launch {
            _vlmModelState.value = VLMModelState.Downloading(0f)
            try {
                DeepSearchEngine.downloadModel().collect { progress ->
                    _vlmModelState.value = VLMModelState.Downloading(progress)
                }
                _vlmModelState.value = VLMModelState.Downloaded
            } catch (e: Exception) {
                _vlmModelState.value = VLMModelState.Error("Download failed: ${e.message}")
            }
        }
    }

    fun loadVLMModel() {
        val ctx = getApplication<android.app.Application>()
        if (!DeepSearchEngine.hasEnoughRAM(ctx)) {
            _vlmModelState.value = VLMModelState.Error("Not enough RAM. Close other apps and try again.")
            return
        }

        viewModelScope.launch {
            _vlmModelState.value = VLMModelState.Loading
            try {
                val success = DeepSearchEngine.loadModel()
                if (success) {
                    _vlmModelState.value = VLMModelState.Ready
                } else {
                    _vlmModelState.value = VLMModelState.Error("Failed to load AI model")
                }
            } catch (e: Exception) {
                _vlmModelState.value = VLMModelState.Error("Load error: ${e.message}")
            }
        }
    }

    fun unloadVLM() {
        viewModelScope.launch {
            DeepSearchEngine.unloadModel()
            // Keep Downloaded state so user knows they don't need to re-download
            if (_vlmModelState.value is VLMModelState.Ready || _vlmModelState.value is VLMModelState.Loading) {
                _vlmModelState.value = VLMModelState.Downloaded
            }
        }
    }

    fun searchGallery(query: String) {
        _deepSearchQuery.value = query
        if (query.isBlank()) {
            _deepSearchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            try {
                val ctx = getApplication<android.app.Application>()
                val results = DeepSearchEngine.searchGallery(query, ctx)
                _deepSearchResults.value = results
            } catch (e: Exception) {
                _deepSearchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun describeFile(file: ScannedFileEntity) {
        _describingFile.value = file
        _vlmStreamText.value = ""

        viewModelScope.launch {
            try {
                DeepSearchEngine.describeImage(
                    file.absolutePath,
                    "Describe this image in detail. What objects, people, text, colors, clothing, and context do you see? Be specific."
                ).collect { token ->
                    _vlmStreamText.value += token
                }

                // Cache the description
                dao.updateVisionDescription(file.absolutePath, _vlmStreamText.value)
            } catch (e: Exception) {
                _vlmStreamText.value += "\n[Error: ${e.message}]"
            }
        }
    }

    fun clearDescribing() {
        _describingFile.value = null
        _vlmStreamText.value = ""
    }

    fun clearSearchResults() {
        _deepSearchResults.value = emptyList()
        _deepSearchQuery.value = ""
    }
}
