package com.sanki.gallery.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sanki.gallery.db.ScannedFileEntity
import java.io.File

// ─────────────────────────────────────────────────────────────────────────────
// Main Results Screen — 4 tabs: All / Waste / Unsure / Important
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorizationList(
    files: List<ScannedFileEntity>,
    wasteCount: Int,
    onCleanTapped: () -> Unit,
    onBack: () -> Unit,
    onMarkKeep: (ScannedFileEntity) -> Unit = {},
    onMarkDelete: (ScannedFileEntity) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Waste", "Unsure", "Important")

    val wasteFiles   = remember(files) { files.filter { it.aiScore <= 25 } }
    val unsureFiles  = remember(files) { files.filter { it.aiScore in 26..65 } }
    val importantFiles = remember(files) { files.filter { it.aiScore > 65 } }
    val totalWasteBytes = remember(wasteFiles) { wasteFiles.sumOf { it.sizeBytes } }

    var isGridView by remember { mutableStateOf(false) }
    var fileToDeleteConfirmation by remember { mutableStateOf<ScannedFileEntity?>(null) }

    val requestDelete: (ScannedFileEntity) -> Unit = { f ->
        if (f.aiScore >= 75 || f.tags.contains("user_kept") || f.tags.contains("faces") || f.tags.contains("mindset_match")) {
            fileToDeleteConfirmation = f
        } else {
            onMarkDelete(f)
        }
    }

    if (fileToDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { fileToDeleteConfirmation = null },
            title = { Text("AI Safety Precaution") },
            text = { Text("The AI believes this file is important (Score: ${fileToDeleteConfirmation?.aiScore}). Are you absolutely sure you want to delete it permanently?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDel = fileToDeleteConfirmation
                        fileToDeleteConfirmation = null
                        if (toDel != null) onMarkDelete(toDel)
                    }
                ) {
                    Text("Delete Permanently", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDeleteConfirmation = null }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        // ── Top Bar ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF111111))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "Scan Results",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF111111)
                )
                Text("${files.size} files analyzed", style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888))
            }
            IconButton(onClick = { isGridView = !isGridView }) {
                Icon(if (isGridView) Icons.Default.List else Icons.Default.Menu, contentDescription = "Toggle View")
            }
        }

        // ── Waste Delete Banner ──────────────────────────────────────────────
        if (wasteCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF3F3))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "$wasteCount junk files",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFB71C1C)
                    )
                    Text(
                        "Free ${Formatter.formatShortFileSize(context, totalWasteBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE53935)
                    )
                }
                Button(
                    onClick = onCleanTapped,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Delete All Junk", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Tabs ─────────────────────────────────────────────────────────────
        TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = Color(0xFF1976D2)) {
            tabs.forEachIndexed { index, title ->
                val count = when (index) { 0 -> files.size; 1 -> wasteFiles.size; 2 -> unsureFiles.size; else -> importantFiles.size }
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            "$title ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // ── Tab Content ───────────────────────────────────────────────────────
        when (selectedTab) {
            0 -> UnifiedFileViewer(files, isGridView, context, onMarkKeep, requestDelete)
            1 -> UnifiedFileViewer(wasteFiles, isGridView, context, onMarkKeep, requestDelete)
            2 -> UnifiedFileViewer(unsureFiles, isGridView, context, onMarkKeep, requestDelete)
            3 -> UnifiedFileViewer(importantFiles, isGridView, context, onMarkKeep, requestDelete)
        }
    }
}

@Composable
fun UnifiedFileViewer(
    files: List<ScannedFileEntity>,
    isGrid: Boolean,
    context: Context,
    onKeep: (ScannedFileEntity) -> Unit,
    onDelete: (ScannedFileEntity) -> Unit
) {
    if (files.isEmpty()) {
        EmptyState("No files here", "You're all clear!")
        return
    }

    if (isGrid) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            gridItems(files, key = { it.absolutePath }) { file ->
                SwipeableFileItem(file = file, isGrid = true, context = context, onKeep = onKeep, onDelete = onDelete)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            listItems(files, key = { it.absolutePath }) { file ->
                SwipeableFileItem(file = file, isGrid = false, context = context, onKeep = onKeep, onDelete = onDelete)
            }
        }
    }
}

@Composable
fun SwipeableFileItem(
    file: ScannedFileEntity,
    isGrid: Boolean,
    context: Context,
    onKeep: (ScannedFileEntity) -> Unit,
    onDelete: (ScannedFileEntity) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = if(isGrid) 100f else 200f
    val height = if (isGrid) 200.dp else 100.dp
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if(isGrid) 4.dp else 0.dp)
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    offsetX > swipeThreshold * 0.5f -> Color(0xFF4CAF50) // Green Keep
                    offsetX < -swipeThreshold * 0.5f -> Color(0xFFE53935) // Red Delete
                    else -> Color(0xFFE0E0E0)
                }
            )
    ) {
        // Icon Under-layer
        Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, contentDescription = "Keep", tint = Color.White, modifier = Modifier.size(32.dp).alpha(if (offsetX > 0) 1f else 0f))
            Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(32.dp).alpha(if (offsetX < 0) 1f else 0f))
        }

        // Top Visible Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offsetX.dp)
                .pointerInput(file.absolutePath) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > swipeThreshold -> onKeep(file)
                                offsetX < -swipeThreshold -> onDelete(file)
                                else -> offsetX = 0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount -> offsetX += dragAmount }
                    )
                }
                .clickable { openFile(context, file.absolutePath) }
                .background(Color.White)
        ) {
            if (isGrid) {
                AsyncImage(
                    model = file.absolutePath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xCC000000)), startY = 100f)))
                Column(Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                    Text(file.name, color = Color.White, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Score: ${file.aiScore}", color = Color.White, fontSize = 10.sp)
                }
            } else {
                Row(Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = file.absolutePath,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(file.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(Formatter.formatShortFileSize(context, file.sizeBytes), style = MaterialTheme.typography.bodySmall)
                        val analysisText = if(file.aiScore > 75) "Important" else if(file.aiScore < 26) "Waste" else "Unsure"
                        Text("Score: ${file.aiScore} - $analysisText", fontSize = 11.sp, color = Color(0xFF888888))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎉", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            if (subtitle.isNotBlank())
                Text(subtitle, color = Color(0xFF888888), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

fun extensionIcon(ext: String): String = when (ext.lowercase()) {
    "pdf" -> "📄"; "docx", "doc" -> "📝"; "xlsx", "xls" -> "📊"
    "mp4", "3gp", "mkv" -> "🎬"; "mp3", "aac", "ogg" -> "🎵"
    "zip", "rar" -> "📦"; else -> "📁"
}

fun openFile(context: Context, path: String) {
    try {
        val file = File(path)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (e: Exception) {
        // Cannot open — ignore silently
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Physical file deletion helper — tries File.delete() then MediaStore fallback
// ─────────────────────────────────────────────────────────────────────────────
fun deleteFilePhysically(context: Context, path: String): Boolean {
    val file = File(path)

    // Primary: standard Java IO (works on Android 11+ with MANAGE_EXTERNAL_STORAGE)
    if (file.exists()) {
        if (file.delete()) return true
    }

    // Fallback: MediaStore delete (works even without full MANAGE permission for media files)
    return try {
        val uri = MediaStore.Files.getContentUri("external")
        val deleted = context.contentResolver.delete(
            uri,
            MediaStore.Files.FileColumns.DATA + "=?",
            arrayOf(path)
        )
        deleted > 0
    } catch (e: Exception) {
        false
    }
}
