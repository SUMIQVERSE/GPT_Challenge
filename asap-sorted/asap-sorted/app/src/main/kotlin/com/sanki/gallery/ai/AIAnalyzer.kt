package com.sanki.gallery.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class AIResult(val score: Int, val tags: List<String>, val textCache: String? = null, val signature: String? = null)

/**
 * On-Device AI Analyzer using Google ML Kit only.
 * Uses pre-installed Google Play Services — zero extra APK size.
 * 100% Offline, 100% Private, runs on even 2GB RAM devices.
 *
 * Scoring Logic:
 *  - Starts at 50 (neutral)
 *  - OCR (text detection) pushes score based on keywords found
 *  - Face detection: many faces = personal photo (high score), no faces + meme text = waste
 *  - Final score clamped between 0–100
 */
class AIAnalyzer(private val context: Context) {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST) // Fast = low battery
            .setMinFaceSize(0.15f)
            .build()
    )

    // Keywords that strongly indicate IMPORTANT (educational/official docs)
    private val importantKeywords = listOf(
        "exam", "syllabus", "result", "notice", "admit", "certificate",
        "fee", "receipt", "invoice", "marksheet", "marks", "grade",
        "aadhaar", "aadhar", "pan card", "passport", "driving licence",
        "university", "college", "school", "admission", "roll no",
        "important", "urgent", "deadline", "salary", "offer letter",
        "bank", "account", "statement", "transaction"
    )

    // Keywords that strongly indicate SPAM/WASTE
    private val wasteKeywords = listOf(
        "forwarded", "good morning", "good night", "happy diwali",
        "happy holi", "jai shree ram", "subh", "shubh", "whatsapp",
        "share karo", "forward", "viral", "joke", "funny",
        "click here", "win prize", "lottery", "free", "offer ends"
    )

    suspend fun analyze(
        bitmap: Bitmap, 
        absolutePath: String,
        wordWeights: Map<String, Int> = emptyMap(),
        signatureWeights: Map<String, Int> = emptyMap()
    ): AIResult {
        var score = 50
        val tags = mutableListOf<String>()
        var foundText: String? = null
        var genSignature: String? = null

        try {
            // === PHASE 1: OCR Text Analysis ===
            val image = InputImage.fromBitmap(bitmap, 0)
            val ocrResult = textRecognizer.process(image).await()
            val detectedText = ocrResult.text.lowercase()
            foundText = detectedText

            if (detectedText.isNotBlank()) {
                tags.add("has_text")

                // Check important keywords
                val importantHits = importantKeywords.count { detectedText.contains(it) }
                val wasteHits = wasteKeywords.count { detectedText.contains(it) }

                when {
                    importantHits >= 2 -> {
                        score += 45
                        tags.add("official_doc")
                    }
                    importantHits == 1 -> {
                        score += 25
                        tags.add("doc")
                    }
                    wasteHits >= 2 -> {
                        score -= 45
                        tags.add("spam")
                    }
                    wasteHits == 1 -> {
                        score -= 25
                        tags.add("possible_spam")
                    }
                }

                // Extra: "Forwarded many times" = instant spam
                if (detectedText.contains("forwarded many times") || detectedText.contains("forwarded")) {
                    score -= 30
                    if (!tags.contains("spam")) tags.add("forwarded")
                }

                // === PHASE 1.5: Offline Local Learning Adjustment ===
                val words = detectedText.split(Regex("\\s+")).filter { it.length > 2 }
                var learningAdjustment = 0
                for (word in words) {
                    val w = word.filter { it.isLetterOrDigit() }
                    if (wordWeights.containsKey(w)) {
                        learningAdjustment += wordWeights[w]!!
                    }
                }
                
                if (learningAdjustment != 0) {
                    Log.d("AIAnalyzer", "Local learning score adjustment: $learningAdjustment")
                    // Cap the personal adjustment so one word doesn't skew everything instantly, but allow strong influence
                    score += learningAdjustment.coerceIn(-30, 30) 
                    tags.add("personalized")
                }

            } else {
                // No text = likely a plain photo/meme
                tags.add("no_text")
            }

            // === PHASE 2: Face Detection ===
            val faces = faceDetector.process(image).await()
            val faceCount = faces.size

            when {
                faceCount >= 3 -> {
                    // Group photo = personal, likely important
                    score += 20
                    tags.add("group_photo")
                }
                faceCount == 1 || faceCount == 2 -> {
                    // Selfie/portrait — keep it
                    score += 10
                    tags.add("portrait")
                }
                faceCount == 0 && detectedText.isBlank() -> {
                    // No text, no faces — likely meme/graphic/irrelevant
                    score -= 15
                    tags.add("no_person")
                }
            }
            
            // === PHASE 3: Superpower Matrix (Context Signature) ===
            // 1. Origin
            val lowerPath = absolutePath.lowercase()
            val origin = when {
                lowerPath.contains("whatsapp") -> "WhatsApp"
                lowerPath.contains("screenshot") -> "Screenshots"
                lowerPath.contains("dcim/camera") -> "Camera"
                lowerPath.contains("download") -> "Downloads"
                else -> "Other"
            }
            
            // 2. Shape
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val shape = when {
                ratio in 0.85f..1.15f -> "SquareMeme"
                ratio < 0.85f -> "PortraitDoc"
                else -> "LandscapePhoto"
            }
            
            // 3. Faces
            val faceTag = if (faceCount > 0) "Faces" else "NoFaces"
            
            // 4. Text Density
            val textDensity = when {
                detectedText.length > 50 -> "TextHeavy"
                detectedText.isNotBlank() -> "LightText"
                else -> "NoText"
            }
            
            val signature = "${origin}_${shape}_${faceTag}_${textDensity}"
            genSignature = signature
            
            // Apply learned mindset weight!
            if (signatureWeights.containsKey(signature)) {
                val sigAdj = signatureWeights[signature]!!
                Log.d("AIAnalyzer", "Applied Context Signature adjustment: $sigAdj for $signature")
                score += sigAdj.coerceIn(-40, 40)
                tags.add("mindset_match")
            }

        } catch (e: Exception) {
            Log.e("AIAnalyzer", "AI analysis error", e)
            tags.add("ai_error")
            // Keep neutral score on error
        }

        val finalScore = score.coerceIn(0, 100)
        return AIResult(finalScore, tags, foundText, genSignature)
    }
}
