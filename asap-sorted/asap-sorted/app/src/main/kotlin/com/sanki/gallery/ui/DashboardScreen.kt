package com.sanki.gallery.ui

import android.text.format.Formatter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    totalBytes: Long,
    usedBytes: Long,
    wasteCount: Int,
    wasteBytes: Long,
    scanState: ScanState,
    hasPermission: Boolean = true,
    onStartScan: () -> Unit,
    onDeleteWaste: () -> Unit,
    onChatWithGallery: () -> Unit = {}
) {
    val context = LocalContext.current
    val usedFraction = if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = usedFraction,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            "Gallery Cleaner",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF111111)
        )
        Text(
            "AI-powered storage optimizer",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(Modifier.height(28.dp))

        // Storage bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Storage Used", color = Color(0xFF444444), style = MaterialTheme.typography.labelMedium)
                    Text(
                        if (totalBytes > 0)
                            "${Formatter.formatShortFileSize(context, usedBytes)} / ${Formatter.formatShortFileSize(context, totalBytes)}"
                        else "Loading...",
                        color = Color(0xFF111111),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (usedFraction > 0.8f) Color(0xFFE53935) else Color(0xFF1976D2))
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "${(usedFraction * 100).toInt()}% used",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Permission warning (shown when MANAGE_EXTERNAL_STORAGE not granted) ──
        if (!hasPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Deletion permission missing",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE65100)
                        )
                        Text(
                            "Tap \"Start Eco-Scan\" to grant it. Without it, app can FIND files but cannot DELETE them.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6D4C41)
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        // Waste summary card (appears after scan)
        if (wasteCount > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "$wasteCount bloat files found",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFB71C1C)
                        )
                        Text(
                            "Can free ${Formatter.formatShortFileSize(context, wasteBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE53935)
                        )
                    }
                    Button(
                        onClick = onDeleteWaste,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Delete All", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // How it works
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "How Eco-Scan works",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF111111)
                )
                Spacer(Modifier.height(8.dp))
                listOf(
                    "1. Folder Rules — Instant 0-battery detection (WhatsApp spam, cache, thumbnails)",
                    "2. Duplicate Hash — Finds exact copies using 128KB fingerprint",
                    "3. AI OCR — Reads text: certificates, notices = important / forwards, memes = waste",
                    "4. AI Face Detection — Group photos = keep, no faces + no text = likely waste"
                ).forEach { line ->
                    Text(
                        line,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF555555),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Premium "Chat with Gallery" Card ──────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onChatWithGallery),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🧠", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Chat with Gallery",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            "Ask AI: \"Kal waali photo dikhao\" — 100% offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xCCFFFFFF),
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        "→",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Scan state UI
        when (scanState) {
            is ScanState.Scanning -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF1976D2), strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Scanning files with AI...",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1565C0),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Running in background. Uses minimal battery.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF888888),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            is ScanState.Error -> {
                Text(
                    "Error: ${(scanState as ScanState.Error).message}",
                    color = Color(0xFFE53935),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onStartScan) { Text("Retry") }
            }
            else -> {
                Button(
                    onClick = onStartScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(
                        if (scanState is ScanState.Done) "Scan Again" else "Start Eco-Scan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}


