package com.example.fitnesstracker.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.example.fitnesstracker.data.entities.Achievement;
import java.lang.Class;
import java.lang.Integer;
import java.lang.Long;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class AchievementDao_Impl implements AchievementDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<Achievement> __insertAdapterOfAchievement;

  public AchievementDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfAchievement = new EntityInsertAdapter<Achievement>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `achievements` (`id`,`title`,`description`,`iconName`,`unlockedAt`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final Achievement entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindText(1, entity.getId());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getTitle());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getDescription());
        }
        if (entity.getIconName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getIconName());
        }
        if (entity.getUnlockedAt() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getUnlockedAt());
        }
      }
    };
  }

  @Override
  public Object insertAll(final List<Achievement> list,
      final Continuation<? super Unit> $completion) {
    if (list == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __insertAdapterOfAchievement.insert(_connection, list);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<Achievement>> getAll() {
    final String _sql = "SELECT * FROM achievements ORDER BY CASE WHEN unlockedAt IS NULL THEN 1 ELSE 0 END, unlockedAt DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"achievements"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfDescription = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "description");
        final int _columnIndexOfIconName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "iconName");
        final int _columnIndexOfUnlockedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "unlockedAt");
        final List<Achievement> _result = new ArrayList<Achievement>();
        while (_stmt.step()) {
          final Achievement _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpDescription;
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null;
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription);
          }
          final String _tmpIconName;
          if (_stmt.isNull(_columnIndexOfIconName)) {
            _tmpIconName = null;
          } else {
            _tmpIconName = _stmt.getText(_columnIndexOfIconName);
          }
          final Long _tmpUnlockedAt;
          if (_stmt.isNull(_columnIndexOfUnlockedAt)) {
            _tmpUnlockedAt = null;
          } else {
            _tmpUnlockedAt = _stmt.getLong(_columnIndexOfUnlockedAt);
          }
          _item = new Achievement(_tmpId,_tmpTitle,_tmpDescription,_tmpIconName,_tmpUnlockedAt);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<List<Achievement>> getUnlocked() {
    final String _sql = "SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"achievements"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfDescription = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "description");
        final int _columnIndexOfIconName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "iconName");
        final int _columnIndexOfUnlockedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "unlockedAt");
        final List<Achievement> _result = new ArrayList<Achievement>();
        while (_stmt.step()) {
          final Achievement _item;
          final String _tmpId;
          if (_stmt.isNull(_columnIndexOfId)) {
            _tmpId = null;
          } else {
            _tmpId = _stmt.getText(_columnIndexOfId);
          }
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpDescription;
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null;
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription);
          }
          final String _tmpIconName;
          if (_stmt.isNull(_columnIndexOfIconName)) {
            _tmpIconName = null;
          } else {
            _tmpIconName = _stmt.getText(_columnIndexOfIconName);
          }
          final Long _tmpUnlockedAt;
          if (_stmt.isNull(_columnIndexOfUnlockedAt)) {
            _tmpUnlockedAt = null;
          } else {
            _tmpUnlockedAt = _stmt.getLong(_columnIndexOfUnlockedAt);
          }
          _item = new Achievement(_tmpId,_tmpTitle,_tmpDescription,_tmpIconName,_tmpUnlockedAt);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM achievements";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final Integer _result;
        if (_stmt.step()) {
          final Integer _tmp;
          if (_stmt.isNull(0)) {
            _tmp = null;
          } else {
            _tmp = (int) (_stmt.getLong(0));
          }
          _result = _tmp;
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object unlock(final String id, final long time,
      final Continuation<? super Unit> $completion) {
    final String _sql = "UPDATE achievements SET unlockedAt = ? WHERE id = ? AND unlockedAt IS NULL";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, time);
        _argIndex = 2;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, id);
        }
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object resetAll(final Continuation<? super Unit> $completion) {
    final String _sql = "UPDATE achievements SET unlockedAt = NULL";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
