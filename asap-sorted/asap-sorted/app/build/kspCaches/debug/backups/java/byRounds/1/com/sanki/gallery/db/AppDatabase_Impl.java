package com.sanki.gallery.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ScannedFileDao _scannedFileDao;

  private volatile AILearningWordDao _aILearningWordDao;

  private volatile AISignatureDao _aISignatureDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(5) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `scanned_files` (`absolutePath` TEXT NOT NULL, `name` TEXT NOT NULL, `sizeBytes` INTEGER NOT NULL, `extension` TEXT NOT NULL, `fileHash` TEXT NOT NULL, `aiScore` INTEGER NOT NULL, `tags` TEXT NOT NULL, `textCache` TEXT, `signature` TEXT, `visionDescription` TEXT, PRIMARY KEY(`absolutePath`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `ai_learning_words` (`word` TEXT NOT NULL, `weight` INTEGER NOT NULL, PRIMARY KEY(`word`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `ai_signature_weights` (`signature` TEXT NOT NULL, `weight` INTEGER NOT NULL, PRIMARY KEY(`signature`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0ec0929bd08d81359435d7ba5fb92a8e')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `scanned_files`");
        db.execSQL("DROP TABLE IF EXISTS `ai_learning_words`");
        db.execSQL("DROP TABLE IF EXISTS `ai_signature_weights`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsScannedFiles = new HashMap<String, TableInfo.Column>(10);
        _columnsScannedFiles.put("absolutePath", new TableInfo.Column("absolutePath", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedFiles.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedFiles.put("sizeBytes", new TableInfo.Column("sizeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedFiles.put("extension", new TableInfo.Column("extension", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedFiles.put("fileHash", new TableInfo.Column("fileHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedFiles.put("aiScore", new TableInfo.Column("aiScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedFiles.put("tags", new TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedFiles.put("textCache", new TableInfo.Column("textCache", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedFiles.put("signature", new TableInfo.Column("signature", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScannedFiles.put("visionDescription", new TableInfo.Column("visionDescription", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysScannedFiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesScannedFiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoScannedFiles = new TableInfo("scanned_files", _columnsScannedFiles, _foreignKeysScannedFiles, _indicesScannedFiles);
        final TableInfo _existingScannedFiles = TableInfo.read(db, "scanned_files");
        if (!_infoScannedFiles.equals(_existingScannedFiles)) {
          return new RoomOpenHelper.ValidationResult(false, "scanned_files(com.sanki.gallery.db.ScannedFileEntity).\n"
                  + " Expected:\n" + _infoScannedFiles + "\n"
                  + " Found:\n" + _existingScannedFiles);
        }
        final HashMap<String, TableInfo.Column> _columnsAiLearningWords = new HashMap<String, TableInfo.Column>(2);
        _columnsAiLearningWords.put("word", new TableInfo.Column("word", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiLearningWords.put("weight", new TableInfo.Column("weight", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAiLearningWords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAiLearningWords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAiLearningWords = new TableInfo("ai_learning_words", _columnsAiLearningWords, _foreignKeysAiLearningWords, _indicesAiLearningWords);
        final TableInfo _existingAiLearningWords = TableInfo.read(db, "ai_learning_words");
        if (!_infoAiLearningWords.equals(_existingAiLearningWords)) {
          return new RoomOpenHelper.ValidationResult(false, "ai_learning_words(com.sanki.gallery.db.AILearningWordEntity).\n"
                  + " Expected:\n" + _infoAiLearningWords + "\n"
                  + " Found:\n" + _existingAiLearningWords);
        }
        final HashMap<String, TableInfo.Column> _columnsAiSignatureWeights = new HashMap<String, TableInfo.Column>(2);
        _columnsAiSignatureWeights.put("signature", new TableInfo.Column("signature", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAiSignatureWeights.put("weight", new TableInfo.Column("weight", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAiSignatureWeights = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAiSignatureWeights = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAiSignatureWeights = new TableInfo("ai_signature_weights", _columnsAiSignatureWeights, _foreignKeysAiSignatureWeights, _indicesAiSignatureWeights);
        final TableInfo _existingAiSignatureWeights = TableInfo.read(db, "ai_signature_weights");
        if (!_infoAiSignatureWeights.equals(_existingAiSignatureWeights)) {
          return new RoomOpenHelper.ValidationResult(false, "ai_signature_weights(com.sanki.gallery.db.AISignatureEntity).\n"
                  + " Expected:\n" + _infoAiSignatureWeights + "\n"
                  + " Found:\n" + _existingAiSignatureWeights);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "0ec0929bd08d81359435d7ba5fb92a8e", "c4dbdb3bb6dab962269f0ed7ba43093f");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "scanned_files","ai_learning_words","ai_signature_weights");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `scanned_files`");
      _db.execSQL("DELETE FROM `ai_learning_words`");
      _db.execSQL("DELETE FROM `ai_signature_weights`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ScannedFileDao.class, ScannedFileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AILearningWordDao.class, AILearningWordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AISignatureDao.class, AISignatureDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ScannedFileDao scannedFileDao() {
    if (_scannedFileDao != null) {
      return _scannedFileDao;
    } else {
      synchronized(this) {
        if(_scannedFileDao == null) {
          _scannedFileDao = new ScannedFileDao_Impl(this);
        }
        return _scannedFileDao;
      }
    }
  }

  @Override
  public AILearningWordDao aiLearningWordDao() {
    if (_aILearningWordDao != null) {
      return _aILearningWordDao;
    } else {
      synchronized(this) {
        if(_aILearningWordDao == null) {
          _aILearningWordDao = new AILearningWordDao_Impl(this);
        }
        return _aILearningWordDao;
      }
    }
  }

  @Override
  public AISignatureDao aiSignatureDao() {
    if (_aISignatureDao != null) {
      return _aISignatureDao;
    } else {
      synchronized(this) {
        if(_aISignatureDao == null) {
          _aISignatureDao = new AISignatureDao_Impl(this);
        }
        return _aISignatureDao;
      }
    }
  }
}
