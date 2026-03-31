package com.sanki.gallery

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sanki.gallery.ui.*

class MainActivity : ComponentActivity() {

    private var permissionGranted = mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        permissionGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check permission on launch
        permissionGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: GalleryViewModel = viewModel()
                    val scanState by vm.scanState.collectAsState()
                    val files by vm.scannedFiles.collectAsState()
                    val wasteCount by vm.wasteFilesCount.collectAsState()
                    val wasteSize by vm.wasteFilesSize.collectAsState()
                    val totalBytes by vm.totalStorageBytes.collectAsState()
                    val usedBytes by vm.usedStorageBytes.collectAsState()

                    // Deep Search states
                    val vlmModelState by vm.vlmModelState.collectAsState()
                    val deepSearchResults by vm.deepSearchResults.collectAsState()
                    val isSearching by vm.isSearching.collectAsState()
                    val vlmStreamText by vm.vlmStreamText.collectAsState()
                    val describingFile by vm.describingFile.collectAsState()

                    var showResults by remember { mutableStateOf(false) }
                    var showDeepSearch by remember { mutableStateOf(false) }
                    val hasPermission by permissionGranted

                    // When scan completes, auto-show results
                    LaunchedEffect(scanState) {
                        if (scanState is ScanState.Done && files.isNotEmpty()) {
                            showResults = true
                        }
                    }

                    when {
                        showDeepSearch -> {
                            DeepSearchScreen(
                                vlmState = vlmModelState,
                                searchResults = deepSearchResults,
                                isSearching = isSearching,
                                vlmStreamText = vlmStreamText,
                                describingFile = describingFile,
                                onBack = {
                                    vm.unloadVLM()
                                    vm.clearDescribing()
                                    vm.clearSearchResults()
                                    showDeepSearch = false
                                },
                                onDownloadModel = { vm.downloadVLMModel() },
                                onLoadModel = { vm.loadVLMModel() },
                                onSearch = { query -> vm.searchGallery(query) },
                                onDescribeFile = { file -> vm.describeFile(file) },
                                onClearDescribing = { vm.clearDescribing() },
                                onOpenFile = { path -> openFile(path) }
                            )
                        }
                        showResults && files.isNotEmpty() -> {
                            CategorizationList(
                                files = files,
                                wasteCount = wasteCount,
                                onCleanTapped = { vm.deleteWasteFiles() },
                                onBack = { showResults = false },
                                onMarkKeep = { vm.markKeep(it) },
                                onMarkDelete = { vm.markDelete(it) }
                            )
                        }
                        else -> {
                            DashboardScreen(
                                totalBytes = totalBytes,
                                usedBytes = usedBytes,
                                wasteCount = wasteCount,
                                wasteBytes = wasteSize,
                                scanState = scanState,
                                hasPermission = hasPermission,
                                onStartScan = {
                                    if (!hasPermission) {
                                        requestStoragePermission()
                                    } else {
                                        vm.startScan()
                                    }
                                },
                                onDeleteWaste = { vm.deleteWasteFiles() },
                                onChatWithGallery = {
                                    vm.checkVLMModelStatus()
                                    showDeepSearch = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                permissionLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                permissionLauncher.launch(intent)
            }
        }
    }

    private fun openFile(path: String) {
        try {
            val file = java.io.File(path)
            val uri = androidx.core.content.FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val mime = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mime)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (e: Exception) {
            Log.w("MainActivity", "Cannot open file: $path", e)
        }
    }

    override fun onResume() {
        super.onResume()
        permissionGranted.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }
}
