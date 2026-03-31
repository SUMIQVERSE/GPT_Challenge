package com.sanki.gallery.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ScannedFileDao_Impl implements ScannedFileDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScannedFileEntity> __insertionAdapterOfScannedFileEntity;

  private final EntityDeletionOrUpdateAdapter<ScannedFileEntity> __deletionAdapterOfScannedFileEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteWaste;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  private final SharedSQLiteStatement __preparedStmtOfUpdateVisionDescription;

  public ScannedFileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScannedFileEntity = new EntityInsertionAdapter<ScannedFileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `scanned_files` (`absolutePath`,`name`,`sizeBytes`,`extension`,`fileHash`,`aiScore`,`tags`,`textCache`,`signature`,`visionDescription`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScannedFileEntity entity) {
        statement.bindString(1, entity.getAbsolutePath());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getSizeBytes());
        statement.bindString(4, entity.getExtension());
        statement.bindString(5, entity.getFileHash());
        statement.bindLong(6, entity.getAiScore());
        statement.bindString(7, entity.getTags());
        if (entity.getTextCache() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getTextCache());
        }
        if (entity.getSignature() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getSignature());
        }
        if (entity.getVisionDescription() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getVisionDescription());
        }
      }
    };
    this.__deletionAdapterOfScannedFileEntity = new EntityDeletionOrUpdateAdapter<ScannedFileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `scanned_files` WHERE `absolutePath` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScannedFileEntity entity) {
        statement.bindString(1, entity.getAbsolutePath());
      }
    };
    this.__preparedStmtOfDeleteWaste = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scanned_files WHERE aiScore <= 25";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scanned_files";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateVisionDescription = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE scanned_files SET visionDescription = ? WHERE absolutePath = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ScannedFileEntity file, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfScannedFileEntity.insert(file);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ScannedFileEntity file, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfScannedFileEntity.handle(file);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteWaste(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteWaste.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteWaste.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateVisionDescription(final String path, final String description,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateVisionDescription.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, description);
        _argIndex = 2;
        _stmt.bindString(_argIndex, path);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateVisionDescription.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ScannedFileEntity>> getAll() {
    final String _sql = "SELECT * FROM scanned_files ORDER BY aiScore ASC, absolutePath ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scanned_files"}, new Callable<List<ScannedFileEntity>>() {
      @Override
      @NonNull
      public List<ScannedFileEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAbsolutePath = CursorUtil.getColumnIndexOrThrow(_cursor, "absolutePath");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfExtension = CursorUtil.getColumnIndexOrThrow(_cursor, "extension");
          final int _cursorIndexOfFileHash = CursorUtil.getColumnIndexOrThrow(_cursor, "fileHash");
          final int _cursorIndexOfAiScore = CursorUtil.getColumnIndexOrThrow(_cursor, "aiScore");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfTextCache = CursorUtil.getColumnIndexOrThrow(_cursor, "textCache");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfVisionDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "visionDescription");
          final List<ScannedFileEntity> _result = new ArrayList<ScannedFileEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScannedFileEntity _item;
            final String _tmpAbsolutePath;
            _tmpAbsolutePath = _cursor.getString(_cursorIndexOfAbsolutePath);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpExtension;
            _tmpExtension = _cursor.getString(_cursorIndexOfExtension);
            final String _tmpFileHash;
            _tmpFileHash = _cursor.getString(_cursorIndexOfFileHash);
            final int _tmpAiScore;
            _tmpAiScore = _cursor.getInt(_cursorIndexOfAiScore);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final String _tmpTextCache;
            if (_cursor.isNull(_cursorIndexOfTextCache)) {
              _tmpTextCache = null;
            } else {
              _tmpTextCache = _cursor.getString(_cursorIndexOfTextCache);
            }
            final String _tmpSignature;
            if (_cursor.isNull(_cursorIndexOfSignature)) {
              _tmpSignature = null;
            } else {
              _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            }
            final String _tmpVisionDescription;
            if (_cursor.isNull(_cursorIndexOfVisionDescription)) {
              _tmpVisionDescription = null;
            } else {
              _tmpVisionDescription = _cursor.getString(_cursorIndexOfVisionDescription);
            }
            _item = new ScannedFileEntity(_tmpAbsolutePath,_tmpName,_tmpSizeBytes,_tmpExtension,_tmpFileHash,_tmpAiScore,_tmpTags,_tmpTextCache,_tmpSignature,_tmpVisionDescription);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getByHash(final String hash,
      final Continuation<? super ScannedFileEntity> $completion) {
    final String _sql = "SELECT * FROM scanned_files WHERE fileHash = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, hash);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ScannedFileEntity>() {
      @Override
      @Nullable
      public ScannedFileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAbsolutePath = CursorUtil.getColumnIndexOrThrow(_cursor, "absolutePath");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfExtension = CursorUtil.getColumnIndexOrThrow(_cursor, "extension");
          final int _cursorIndexOfFileHash = CursorUtil.getColumnIndexOrThrow(_cursor, "fileHash");
          final int _cursorIndexOfAiScore = CursorUtil.getColumnIndexOrThrow(_cursor, "aiScore");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfTextCache = CursorUtil.getColumnIndexOrThrow(_cursor, "textCache");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfVisionDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "visionDescription");
          final ScannedFileEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpAbsolutePath;
            _tmpAbsolutePath = _cursor.getString(_cursorIndexOfAbsolutePath);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpExtension;
            _tmpExtension = _cursor.getString(_cursorIndexOfExtension);
            final String _tmpFileHash;
            _tmpFileHash = _cursor.getString(_cursorIndexOfFileHash);
            final int _tmpAiScore;
            _tmpAiScore = _cursor.getInt(_cursorIndexOfAiScore);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final String _tmpTextCache;
            if (_cursor.isNull(_cursorIndexOfTextCache)) {
              _tmpTextCache = null;
            } else {
              _tmpTextCache = _cursor.getString(_cursorIndexOfTextCache);
            }
            final String _tmpSignature;
            if (_cursor.isNull(_cursorIndexOfSignature)) {
              _tmpSignature = null;
            } else {
              _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            }
            final String _tmpVisionDescription;
            if (_cursor.isNull(_cursorIndexOfVisionDescription)) {
              _tmpVisionDescription = null;
            } else {
              _tmpVisionDescription = _cursor.getString(_cursorIndexOfVisionDescription);
            }
            _result = new ScannedFileEntity(_tmpAbsolutePath,_tmpName,_tmpSizeBytes,_tmpExtension,_tmpFileHash,_tmpAiScore,_tmpTags,_tmpTextCache,_tmpSignature,_tmpVisionDescription);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getWasteFiles(final Continuation<? super List<ScannedFileEntity>> $completion) {
    final String _sql = "SELECT * FROM scanned_files WHERE aiScore <= 25";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScannedFileEntity>>() {
      @Override
      @NonNull
      public List<ScannedFileEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAbsolutePath = CursorUtil.getColumnIndexOrThrow(_cursor, "absolutePath");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfExtension = CursorUtil.getColumnIndexOrThrow(_cursor, "extension");
          final int _cursorIndexOfFileHash = CursorUtil.getColumnIndexOrThrow(_cursor, "fileHash");
          final int _cursorIndexOfAiScore = CursorUtil.getColumnIndexOrThrow(_cursor, "aiScore");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfTextCache = CursorUtil.getColumnIndexOrThrow(_cursor, "textCache");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfVisionDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "visionDescription");
          final List<ScannedFileEntity> _result = new ArrayList<ScannedFileEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScannedFileEntity _item;
            final String _tmpAbsolutePath;
            _tmpAbsolutePath = _cursor.getString(_cursorIndexOfAbsolutePath);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpExtension;
            _tmpExtension = _cursor.getString(_cursorIndexOfExtension);
            final String _tmpFileHash;
            _tmpFileHash = _cursor.getString(_cursorIndexOfFileHash);
            final int _tmpAiScore;
            _tmpAiScore = _cursor.getInt(_cursorIndexOfAiScore);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final String _tmpTextCache;
            if (_cursor.isNull(_cursorIndexOfTextCache)) {
              _tmpTextCache = null;
            } else {
              _tmpTextCache = _cursor.getString(_cursorIndexOfTextCache);
            }
            final String _tmpSignature;
            if (_cursor.isNull(_cursorIndexOfSignature)) {
              _tmpSignature = null;
            } else {
              _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            }
            final String _tmpVisionDescription;
            if (_cursor.isNull(_cursorIndexOfVisionDescription)) {
              _tmpVisionDescription = null;
            } else {
              _tmpVisionDescription = _cursor.getString(_cursorIndexOfVisionDescription);
            }
            _item = new ScannedFileEntity(_tmpAbsolutePath,_tmpName,_tmpSizeBytes,_tmpExtension,_tmpFileHash,_tmpAiScore,_tmpTags,_tmpTextCache,_tmpSignature,_tmpVisionDescription);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object searchByText(final String query,
      final Continuation<? super List<ScannedFileEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM scanned_files \n"
            + "        WHERE textCache LIKE ? \n"
            + "           OR tags LIKE ? \n"
            + "           OR visionDescription LIKE ? \n"
            + "           OR name LIKE ?\n"
            + "        ORDER BY aiScore DESC\n"
            + "        LIMIT 50\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    _argIndex = 4;
    _statement.bindString(_argIndex, query);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScannedFileEntity>>() {
      @Override
      @NonNull
      public List<ScannedFileEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAbsolutePath = CursorUtil.getColumnIndexOrThrow(_cursor, "absolutePath");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfExtension = CursorUtil.getColumnIndexOrThrow(_cursor, "extension");
          final int _cursorIndexOfFileHash = CursorUtil.getColumnIndexOrThrow(_cursor, "fileHash");
          final int _cursorIndexOfAiScore = CursorUtil.getColumnIndexOrThrow(_cursor, "aiScore");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfTextCache = CursorUtil.getColumnIndexOrThrow(_cursor, "textCache");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfVisionDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "visionDescription");
          final List<ScannedFileEntity> _result = new ArrayList<ScannedFileEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScannedFileEntity _item;
            final String _tmpAbsolutePath;
            _tmpAbsolutePath = _cursor.getString(_cursorIndexOfAbsolutePath);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpExtension;
            _tmpExtension = _cursor.getString(_cursorIndexOfExtension);
            final String _tmpFileHash;
            _tmpFileHash = _cursor.getString(_cursorIndexOfFileHash);
            final int _tmpAiScore;
            _tmpAiScore = _cursor.getInt(_cursorIndexOfAiScore);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final String _tmpTextCache;
            if (_cursor.isNull(_cursorIndexOfTextCache)) {
              _tmpTextCache = null;
            } else {
              _tmpTextCache = _cursor.getString(_cursorIndexOfTextCache);
            }
            final String _tmpSignature;
            if (_cursor.isNull(_cursorIndexOfSignature)) {
              _tmpSignature = null;
            } else {
              _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            }
            final String _tmpVisionDescription;
            if (_cursor.isNull(_cursorIndexOfVisionDescription)) {
              _tmpVisionDescription = null;
            } else {
              _tmpVisionDescription = _cursor.getString(_cursorIndexOfVisionDescription);
            }
            _item = new ScannedFileEntity(_tmpAbsolutePath,_tmpName,_tmpSizeBytes,_tmpExtension,_tmpFileHash,_tmpAiScore,_tmpTags,_tmpTextCache,_tmpSignature,_tmpVisionDescription);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getUndescribedImages(final int limit,
      final Continuation<? super List<ScannedFileEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM scanned_files \n"
            + "        WHERE visionDescription IS NULL \n"
            + "          AND extension IN ('jpg', 'jpeg', 'png', 'webp', 'gif', 'pdf')\n"
            + "        ORDER BY sizeBytes DESC\n"
            + "        LIMIT ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScannedFileEntity>>() {
      @Override
      @NonNull
      public List<ScannedFileEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAbsolutePath = CursorUtil.getColumnIndexOrThrow(_cursor, "absolutePath");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfExtension = CursorUtil.getColumnIndexOrThrow(_cursor, "extension");
          final int _cursorIndexOfFileHash = CursorUtil.getColumnIndexOrThrow(_cursor, "fileHash");
          final int _cursorIndexOfAiScore = CursorUtil.getColumnIndexOrThrow(_cursor, "aiScore");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfTextCache = CursorUtil.getColumnIndexOrThrow(_cursor, "textCache");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfVisionDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "visionDescription");
          final List<ScannedFileEntity> _result = new ArrayList<ScannedFileEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScannedFileEntity _item;
            final String _tmpAbsolutePath;
            _tmpAbsolutePath = _cursor.getString(_cursorIndexOfAbsolutePath);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpExtension;
            _tmpExtension = _cursor.getString(_cursorIndexOfExtension);
            final String _tmpFileHash;
            _tmpFileHash = _cursor.getString(_cursorIndexOfFileHash);
            final int _tmpAiScore;
            _tmpAiScore = _cursor.getInt(_cursorIndexOfAiScore);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final String _tmpTextCache;
            if (_cursor.isNull(_cursorIndexOfTextCache)) {
              _tmpTextCache = null;
            } else {
              _tmpTextCache = _cursor.getString(_cursorIndexOfTextCache);
            }
            final String _tmpSignature;
            if (_cursor.isNull(_cursorIndexOfSignature)) {
              _tmpSignature = null;
            } else {
              _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            }
            final String _tmpVisionDescription;
            if (_cursor.isNull(_cursorIndexOfVisionDescription)) {
              _tmpVisionDescription = null;
            } else {
              _tmpVisionDescription = _cursor.getString(_cursorIndexOfVisionDescription);
            }
            _item = new ScannedFileEntity(_tmpAbsolutePath,_tmpName,_tmpSizeBytes,_tmpExtension,_tmpFileHash,_tmpAiScore,_tmpTags,_tmpTextCache,_tmpSignature,_tmpVisionDescription);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
