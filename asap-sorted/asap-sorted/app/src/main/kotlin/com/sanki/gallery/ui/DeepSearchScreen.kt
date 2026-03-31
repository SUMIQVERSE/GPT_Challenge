package com.sanki.gallery.ui

import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sanki.gallery.db.ScannedFileEntity

// ─────────────────────────────────────────────────────────────────────────────
// "Chat with Gallery" — Premium Deep Search Screen
// Powered by RunAnywhere SDK's on-device VLM (100% offline, 100% private)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepSearchScreen(
    vlmState: VLMModelState,
    searchResults: List<ScannedFileEntity>,
    isSearching: Boolean,
    vlmStreamText: String,
    describingFile: ScannedFileEntity?,
    onBack: () -> Unit,
    onDownloadModel: () -> Unit,
    onLoadModel: () -> Unit,
    onSearch: (String) -> Unit,
    onDescribeFile: (ScannedFileEntity) -> Unit,
    onClearDescribing: () -> Unit,
    onOpenFile: (String) -> Unit
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Top Bar ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF111111))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "Chat with Gallery",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF111111)
                )
                Text(
                    "AI understands your photos • 100% offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
            }
        }

        // ── Model Status Card ───────────────────────────────────────────────
        when (vlmState) {
            is VLMModelState.NotDownloaded -> {
                ModelStatusCard(
                    emoji = "🧠",
                    title = "Download AI Brain",
                    subtitle = "SmolVLM — 256MB one-time download.\nAfter this, works 100% offline forever.",
                    buttonText = "Download Now",
                    buttonColor = Color(0xFF1976D2),
                    onClick = onDownloadModel
                )
            }

            is VLMModelState.Downloading -> {
                val progress by animateFloatAsState(
                    targetValue = vlmState.progress,
                    animationSpec = tween(300),
                    label = "dlProgress"
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(28.dp),
                                color = Color(0xFF1976D2),
                                strokeWidth = 3.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Downloading AI Brain...",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1565C0)
                                )
                                Text(
                                    "${(progress * 100).toInt()}% complete",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF42A5F5)
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF1976D2),
                            trackColor = Color(0xFFBBDEFB)
                        )
                    }
                }
            }

            is VLMModelState.Downloaded -> {
                ModelStatusCard(
                    emoji = "✅",
                    title = "AI Brain Ready",
                    subtitle = "Tap to activate. Uses ~365MB RAM temporarily.",
                    buttonText = "Activate AI",
                    buttonColor = Color(0xFF43A047),
                    onClick = onLoadModel
                )
            }

            is VLMModelState.Loading -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF7B1FA2),
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Loading AI into memory...",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF7B1FA2)
                            )
                            Text(
                                "This takes 2-5 seconds",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFAB47BC)
                            )
                        }
                    }
                }
            }

            is VLMModelState.Ready -> {
                // Search bar + ready indicator
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🟢", fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "AI Active — Ask anything about your photos",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            is VLMModelState.Error -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "⚠️ ${vlmState.message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB71C1C)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onLoadModel,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Retry", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // ── Search Bar (visible when model is Ready OR Downloaded/has cached data) ────
        if (vlmState is VLMModelState.Ready || vlmState is VLMModelState.Downloaded) {
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = {
                    Text(
                        "\"Black shirt ki photo\" ya \"Electricity bill PDF\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFAAAAAA)
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF888888))
                },
                trailingIcon = {
                    if (searchText.isNotBlank()) {
                        IconButton(onClick = {
                            searchText = ""
                            onSearch("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF888888))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5)
                )
            )

            Spacer(Modifier.height(4.dp))

            // Search button
            if (searchText.isNotBlank()) {
                Button(
                    onClick = { onSearch(searchText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Searching...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Text("🔍 Search Gallery", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // ── Quick Suggestions ───────────────────────────────────────────────
        if (vlmState is VLMModelState.Ready && searchText.isBlank() && searchResults.isEmpty() && describingFile == null) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Try asking:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF666666),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(6.dp))
            listOf(
                "📄 Last month ki electricity bill",
                "👕 Black shirt waali photo",
                "🎓 College fee receipt",
                "📸 Group photo with friends",
                "📋 Aadhaar card ki photo"
            ).forEach { suggestion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp)
                        .clickable {
                            searchText = suggestion.substringAfter(" ")
                            onSearch(searchText)
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Text(
                        suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF444444),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── File Description Overlay ────────────────────────────────────────
        AnimatedVisibility(
            visible = describingFile != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            describingFile?.let { file ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(min = 200.dp, max = 400.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = file.absolutePath,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    file.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "🧠 AI Vision Analysis",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF7B1FA2)
                                )
                            }
                            IconButton(onClick = onClearDescribing) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF888888))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Divider(color = Color(0xFFE0E0E0))
                        Spacer(Modifier.height(8.dp))

                        // Streaming text
                        Column(
                            Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (vlmStreamText.isBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color(0xFF7B1FA2),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "AI is looking at this image...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF888888)
                                    )
                                }
                            } else {
                                Text(
                                    vlmStreamText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF333333),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Search Results Grid ─────────────────────────────────────────────
        if (searchResults.isNotEmpty() && describingFile == null) {
            Text(
                "${searchResults.size} results found",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF666666),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults, key = { it.absolutePath }) { file ->
                    SearchResultCard(
                        file = file,
                        context = context,
                        vlmReady = vlmState is VLMModelState.Ready,
                        onTap = { onOpenFile(file.absolutePath) },
                        onDescribe = { onDescribeFile(file) }
                    )
                }
            }
        }

        // ── Empty state after search ────────────────────────────────────────
        if (searchResults.isEmpty() && isSearching) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color(0xFF1976D2),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Searching through your files...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF888888),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    file: ScannedFileEntity,
    context: android.content.Context,
    vlmReady: Boolean,
    onTap: () -> Unit,
    onDescribe: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = file.absolutePath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay at bottom
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC000000)),
                            startY = 80f
                        )
                    )
            )

            // File info
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    file.name,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
                Text(
                    Formatter.formatShortFileSize(context, file.sizeBytes),
                    color = Color(0xFFCCCCCC),
                    fontSize = 10.sp
                )
            }

            // "Ask AI" button (only when VLM is ready)
            if (vlmReady) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC7B1FA2))
                        .clickable(onClick = onDescribe)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "🧠 Ask",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelStatusCard(
    emoji: String,
    title: String,
    subtitle: String,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 36.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF111111),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
