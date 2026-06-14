package com.example.fitnesstracker.data.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.example.fitnesstracker.data.entities.SleepRecord;
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
public final class SleepDao_Impl implements SleepDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<SleepRecord> __insertAdapterOfSleepRecord;

  private final EntityDeleteOrUpdateAdapter<SleepRecord> __deleteAdapterOfSleepRecord;

  public SleepDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfSleepRecord = new EntityInsertAdapter<SleepRecord>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sleep_records` (`id`,`bedTime`,`wakeTime`,`qualityScore`,`tags`,`note`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final SleepRecord entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getBedTime());
        statement.bindLong(3, entity.getWakeTime());
        statement.bindLong(4, entity.getQualityScore());
        if (entity.getTags() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getTags());
        }
        if (entity.getNote() == null) {
          statement.bindNull(6);
        } else {
          statement.bindText(6, entity.getNote());
        }
      }
    };
    this.__deleteAdapterOfSleepRecord = new EntityDeleteOrUpdateAdapter<SleepRecord>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `sleep_records` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final SleepRecord entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final SleepRecord record, final Continuation<? super Long> $completion) {
    if (record == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfSleepRecord.insertAndReturnId(_connection, record);
    }, $completion);
  }

  @Override
  public Object delete(final SleepRecord record, final Continuation<? super Unit> $completion) {
    if (record == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __deleteAdapterOfSleepRecord.handle(_connection, record);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<SleepRecord>> getAll() {
    final String _sql = "SELECT * FROM sleep_records ORDER BY bedTime DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"sleep_records"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfBedTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "bedTime");
        final int _columnIndexOfWakeTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "wakeTime");
        final int _columnIndexOfQualityScore = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "qualityScore");
        final int _columnIndexOfTags = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "tags");
        final int _columnIndexOfNote = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "note");
        final List<SleepRecord> _result = new ArrayList<SleepRecord>();
        while (_stmt.step()) {
          final SleepRecord _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final long _tmpBedTime;
          _tmpBedTime = _stmt.getLong(_columnIndexOfBedTime);
          final long _tmpWakeTime;
          _tmpWakeTime = _stmt.getLong(_columnIndexOfWakeTime);
          final int _tmpQualityScore;
          _tmpQualityScore = (int) (_stmt.getLong(_columnIndexOfQualityScore));
          final String _tmpTags;
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null;
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags);
          }
          final String _tmpNote;
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null;
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote);
          }
          _item = new SleepRecord(_tmpId,_tmpBedTime,_tmpWakeTime,_tmpQualityScore,_tmpTags,_tmpNote);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<SleepRecord> getForDay(final long dayStart, final long dayEnd) {
    final String _sql = "SELECT * FROM sleep_records WHERE bedTime >= ? AND bedTime < ? LIMIT 1";
    return FlowUtil.createFlow(__db, false, new String[] {"sleep_records"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, dayStart);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, dayEnd);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfBedTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "bedTime");
        final int _columnIndexOfWakeTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "wakeTime");
        final int _columnIndexOfQualityScore = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "qualityScore");
        final int _columnIndexOfTags = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "tags");
        final int _columnIndexOfNote = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "note");
        final SleepRecord _result;
        if (_stmt.step()) {
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final long _tmpBedTime;
          _tmpBedTime = _stmt.getLong(_columnIndexOfBedTime);
          final long _tmpWakeTime;
          _tmpWakeTime = _stmt.getLong(_columnIndexOfWakeTime);
          final int _tmpQualityScore;
          _tmpQualityScore = (int) (_stmt.getLong(_columnIndexOfQualityScore));
          final String _tmpTags;
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null;
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags);
          }
          final String _tmpNote;
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null;
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote);
          }
          _result = new SleepRecord(_tmpId,_tmpBedTime,_tmpWakeTime,_tmpQualityScore,_tmpTags,_tmpNote);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Flow<SleepRecord> getLatest() {
    final String _sql = "SELECT * FROM sleep_records ORDER BY bedTime DESC LIMIT 1";
    return FlowUtil.createFlow(__db, false, new String[] {"sleep_records"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfBedTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "bedTime");
        final int _columnIndexOfWakeTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "wakeTime");
        final int _columnIndexOfQualityScore = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "qualityScore");
        final int _columnIndexOfTags = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "tags");
        final int _columnIndexOfNote = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "note");
        final SleepRecord _result;
        if (_stmt.step()) {
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final long _tmpBedTime;
          _tmpBedTime = _stmt.getLong(_columnIndexOfBedTime);
          final long _tmpWakeTime;
          _tmpWakeTime = _stmt.getLong(_columnIndexOfWakeTime);
          final int _tmpQualityScore;
          _tmpQualityScore = (int) (_stmt.getLong(_columnIndexOfQualityScore));
          final String _tmpTags;
          if (_stmt.isNull(_columnIndexOfTags)) {
            _tmpTags = null;
          } else {
            _tmpTags = _stmt.getText(_columnIndexOfTags);
          }
          final String _tmpNote;
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null;
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote);
          }
          _result = new SleepRecord(_tmpId,_tmpBedTime,_tmpWakeTime,_tmpQualityScore,_tmpTags,_tmpNote);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM sleep_records";
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
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    final String _sql = "DELETE FROM sleep_records";
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
