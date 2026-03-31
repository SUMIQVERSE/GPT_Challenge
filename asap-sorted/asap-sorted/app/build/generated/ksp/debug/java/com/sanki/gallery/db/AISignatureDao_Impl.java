package com.sanki.gallery.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AISignatureDao_Impl implements AISignatureDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfIncrementSignature;

  private final SharedSQLiteStatement __preparedStmtOfDecrementSignature;

  public AISignatureDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfIncrementSignature = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "INSERT OR REPLACE INTO ai_signature_weights (signature, weight) VALUES (?, COALESCE((SELECT weight FROM ai_signature_weights WHERE signature = ?), 0) + 5)";
        return _query;
      }
    };
    this.__preparedStmtOfDecrementSignature = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "INSERT OR REPLACE INTO ai_signature_weights (signature, weight) VALUES (?, COALESCE((SELECT weight FROM ai_signature_weights WHERE signature = ?), 0) - 5)";
        return _query;
      }
    };
  }

  @Override
  public Object incrementSignature(final String sig, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementSignature.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sig);
        _argIndex = 2;
        _stmt.bindString(_argIndex, sig);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeInsert();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfIncrementSignature.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object decrementSignature(final String sig, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDecrementSignature.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sig);
        _argIndex = 2;
        _stmt.bindString(_argIndex, sig);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeInsert();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDecrementSignature.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllWeights(final Continuation<? super List<AISignatureEntity>> $completion) {
    final String _sql = "SELECT * FROM ai_signature_weights";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AISignatureEntity>>() {
      @Override
      @NonNull
      public List<AISignatureEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "weight");
          final List<AISignatureEntity> _result = new ArrayList<AISignatureEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AISignatureEntity _item;
            final String _tmpSignature;
            _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            final int _tmpWeight;
            _tmpWeight = _cursor.getInt(_cursorIndexOfWeight);
            _item = new AISignatureEntity(_tmpSignature,_tmpWeight);
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
