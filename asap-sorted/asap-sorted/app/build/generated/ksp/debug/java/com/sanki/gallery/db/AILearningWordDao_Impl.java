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
public final class AILearningWordDao_Impl implements AILearningWordDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfIncrementWeight;

  private final SharedSQLiteStatement __preparedStmtOfDecrementWeight;

  public AILearningWordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfIncrementWeight = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "INSERT OR REPLACE INTO ai_learning_words (word, weight) VALUES (?, COALESCE((SELECT weight FROM ai_learning_words WHERE word = ?), 0) + 1)";
        return _query;
      }
    };
    this.__preparedStmtOfDecrementWeight = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "INSERT OR REPLACE INTO ai_learning_words (word, weight) VALUES (?, COALESCE((SELECT weight FROM ai_learning_words WHERE word = ?), 0) - 1)";
        return _query;
      }
    };
  }

  @Override
  public Object incrementWeight(final String word, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementWeight.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, word);
        _argIndex = 2;
        _stmt.bindString(_argIndex, word);
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
          __preparedStmtOfIncrementWeight.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object decrementWeight(final String word, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDecrementWeight.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, word);
        _argIndex = 2;
        _stmt.bindString(_argIndex, word);
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
          __preparedStmtOfDecrementWeight.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllWeights(final Continuation<? super List<AILearningWordEntity>> $completion) {
    final String _sql = "SELECT * FROM ai_learning_words";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AILearningWordEntity>>() {
      @Override
      @NonNull
      public List<AILearningWordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "weight");
          final List<AILearningWordEntity> _result = new ArrayList<AILearningWordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AILearningWordEntity _item;
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final int _tmpWeight;
            _tmpWeight = _cursor.getInt(_cursorIndexOfWeight);
            _item = new AILearningWordEntity(_tmpWord,_tmpWeight);
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
