package com.sanki.gallery.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "scanned_files")
data class ScannedFileEntity(
    @PrimaryKey val absolutePath: String,
    val name: String,
    val sizeBytes: Long,
    val extension: String,
    val fileHash: String,
    val aiScore: Int, // 0 to 100
    val tags: String, // comma separated
    val textCache: String? = null, // For offline AI learning, temporary extraction
    val signature: String? = null, // Context Signature e.g. "WhatsApp_Square_NoFaces_NoText"
    val visionDescription: String? = null // VLM-generated description for Deep Search
)

@Entity(tableName = "ai_learning_words")
data class AILearningWordEntity(
    @PrimaryKey val word: String,
    val weight: Int // positive = important, negative = waste
)

@Entity(tableName = "ai_signature_weights")
data class AISignatureEntity(
    @PrimaryKey val signature: String,
    val weight: Int // positive = important, negative = waste
)

@Dao
interface ScannedFileDao {
    @Query("SELECT * FROM scanned_files ORDER BY aiScore ASC, absolutePath ASC")
    fun getAll(): Flow<List<ScannedFileEntity>>

    @Query("SELECT * FROM scanned_files WHERE fileHash = :hash LIMIT 1")
    suspend fun getByHash(hash: String): ScannedFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: ScannedFileEntity)

    @Delete
    suspend fun delete(file: ScannedFileEntity)
    
    @Query("DELETE FROM scanned_files WHERE aiScore <= 25")
    suspend fun deleteWaste()

    @Query("SELECT * FROM scanned_files WHERE aiScore <= 25")
    suspend fun getWasteFiles(): List<ScannedFileEntity>

    @Query("DELETE FROM scanned_files")
    suspend fun clearAll()

    // ── Deep Search Queries ──────────────────────────────────────────────────

    @Query("""
        SELECT * FROM scanned_files 
        WHERE textCache LIKE :query 
           OR tags LIKE :query 
           OR visionDescription LIKE :query 
           OR name LIKE :query
        ORDER BY aiScore DESC
        LIMIT 50
    """)
    suspend fun searchByText(query: String): List<ScannedFileEntity>

    @Query("""
        SELECT * FROM scanned_files 
        WHERE visionDescription IS NULL 
          AND extension IN ('jpg', 'jpeg', 'png', 'webp', 'gif', 'pdf')
        ORDER BY sizeBytes DESC
        LIMIT :limit
    """)
    suspend fun getUndescribedImages(limit: Int): List<ScannedFileEntity>

    @Query("UPDATE scanned_files SET visionDescription = :description WHERE absolutePath = :path")
    suspend fun updateVisionDescription(path: String, description: String)
}

@Dao
interface AILearningWordDao {
    @Query("SELECT * FROM ai_learning_words")
    suspend fun getAllWeights(): List<AILearningWordEntity>

    @Query("INSERT OR REPLACE INTO ai_learning_words (word, weight) VALUES (:word, COALESCE((SELECT weight FROM ai_learning_words WHERE word = :word), 0) + 1)")
    suspend fun incrementWeight(word: String)

    @Query("INSERT OR REPLACE INTO ai_learning_words (word, weight) VALUES (:word, COALESCE((SELECT weight FROM ai_learning_words WHERE word = :word), 0) - 1)")
    suspend fun decrementWeight(word: String)
}

@Dao
interface AISignatureDao {
    @Query("SELECT * FROM ai_signature_weights")
    suspend fun getAllWeights(): List<AISignatureEntity>

    @Query("INSERT OR REPLACE INTO ai_signature_weights (signature, weight) VALUES (:sig, COALESCE((SELECT weight FROM ai_signature_weights WHERE signature = :sig), 0) + 5)")
    suspend fun incrementSignature(sig: String)

    @Query("INSERT OR REPLACE INTO ai_signature_weights (signature, weight) VALUES (:sig, COALESCE((SELECT weight FROM ai_signature_weights WHERE signature = :sig), 0) - 5)")
    suspend fun decrementSignature(sig: String)
}

@Database(entities = [ScannedFileEntity::class, AILearningWordEntity::class, AISignatureEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scannedFileDao(): ScannedFileDao
    abstract fun aiLearningWordDao(): AILearningWordDao
    abstract fun aiSignatureDao(): AISignatureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gallery_smart_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
